package com.homes.backend.domain.user.service;

import com.homes.backend.domain.user.dto.request.UserCreateReqDto;
import com.homes.backend.domain.user.dto.response.UserSignupResDto;
import com.homes.backend.domain.user.entity.User;
import com.homes.backend.domain.user.repository.UserRepository;
import com.homes.backend.domain.user.exception.UserErrorCode;
import com.homes.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public void checkEmailDuplication(String email) {
        if (userRepository.existsByEmail(email)) {
            // 중복 시 에러 코드 실어 핸들러가 가로챔
            throw new CustomException(UserErrorCode.DUPLICATE_EMAIL);
        }
    }

    @Transactional
    public UserSignupResDto signUp(UserCreateReqDto request) {
        // 1. 이메일 중복 검사
        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException(UserErrorCode.DUPLICATE_EMAIL); //이메일 있으면 에러
        }

        // 2. 엔티티 생성 및 저장 (실제로는 암호화 로직 필요)
        User user = User.builder() //빌더 패턴 사용
                .email(request.email())
                .password(request.password()) // 나중에 Security 설정 후 암호화 예정
                .build();

        User savedUser = userRepository.save(user);

        // 3. 응답 DTO로 변환하여 반환
        return UserSignupResDto.from(savedUser);
    }



}