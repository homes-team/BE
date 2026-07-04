package com.homes.backend.domain.realtor.service;

import com.homes.backend.domain.realtor.dto.request.RealtorSignupReqDto;
import com.homes.backend.domain.realtor.dto.response.RealtorSignupResDto;
import com.homes.backend.domain.realtor.entity.Agent;
import com.homes.backend.domain.realtor.exception.RealtorErrorCode;
import com.homes.backend.domain.realtor.repository.AgentRepository;
import com.homes.backend.domain.user.entity.User;
import com.homes.backend.domain.user.exception.UserErrorCode;
import com.homes.backend.domain.user.repository.UserRepository;
import com.homes.backend.global.exception.CustomException;
import com.homes.backend.global.util.LocalFileUploader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RealtorService {

    private final UserRepository userRepository;
    private final AgentRepository agentRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final LocalFileUploader localFileUploader;

    @Transactional
    public RealtorSignupResDto signUp(
            RealtorSignupReqDto request,
            MultipartFile businessCertImage,
            MultipartFile agentCertImage,
            MultipartFile profileImage
    ) {
        // 1. 이메일 DB 중복 검사
        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException(UserErrorCode.DUPLICATE_EMAIL);
        }

        // 2. 사전에 완료된 이메일 인증인지 확인 (일반 회원가입과 동일한 인증 절차)
        String isVerified = (String) redisTemplate.opsForValue().get("AUTH_SUCCESS:" + request.email());
        if (isVerified == null || !isVerified.equals("TRUE")) {
            throw new CustomException(UserErrorCode.EMAIL_NOT_VERIFIED);
        }

        // 3. 사업자등록번호 중복 검사
        if (agentRepository.existsByBusinessNum(request.businessNum())) {
            throw new CustomException(RealtorErrorCode.DUPLICATE_BUSINESS_NUM);
        }

        // 4. 유저 계정 생성
        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .phone(request.phone())
                .role("AGENT")
                .build();
        User savedUser = userRepository.save(user);

        // 5. 서류 이미지 업로드 (S3 연동 전까지는 로컬 업로더 사용)
        String businessCertUrl = localFileUploader.upload(businessCertImage, "agent-certs");
        String agentCertUrl = localFileUploader.upload(agentCertImage, "agent-certs");
        String profileImageUrl = localFileUploader.upload(profileImage, "agent-profiles");

        // 6. 중개사 프로필 생성 (관리자 승인 전까지 isVerified=false로 대기)
        Agent agent = Agent.builder()
                .user(savedUser)
                .businessNum(request.businessNum())
                .officeName(request.officeName())
                .businessCertUrl(businessCertUrl)
                .agentCertUrl(agentCertUrl)
                .profileImageUrl(profileImageUrl)
                .build();
        Agent savedAgent = agentRepository.save(agent);

        // 가입에 사용된 이메일 인증 증표는 파기
        redisTemplate.delete("AUTH_SUCCESS:" + request.email());

        return RealtorSignupResDto.from(savedUser, savedAgent);
    }
}
