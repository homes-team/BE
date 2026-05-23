package com.homes.backend.domain.user.service;

import com.homes.backend.domain.user.dto.request.UserCreateReqDto;
import com.homes.backend.domain.user.dto.request.UserLoginReqDto;
import com.homes.backend.domain.user.dto.request.UserUpdatePasswordReqDto;
import com.homes.backend.domain.user.dto.response.UserSignupResDto;
import com.homes.backend.domain.user.entity.User;
import com.homes.backend.domain.user.repository.UserRepository;
import com.homes.backend.domain.user.exception.UserErrorCode;
import com.homes.backend.global.exception.CustomException;
import com.homes.backend.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // 💡 1. 주입받기
    private final JwtTokenProvider jwtTokenProvider; // 💡 2. 주입받기

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
                //사용자가 친 평문 비밀번호를 BCrypt로 난수화하여 암호화 저장
                .password(passwordEncoder.encode(request.password()))
                .build();

        User savedUser = userRepository.save(user);

        // 3. 응답 DTO로 변환하여 반환
        return UserSignupResDto.from(savedUser);
    }
    
    public String login(UserLoginReqDto request) {
        // 1. DB에서 이메일로 유저 찾기
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 2. 입력한 비밀번호와 DB에 암호화되어 저장된 비밀번호가 일치하는지 대조
        // 시큐리티가 제공하는 matches 메서드를 써야 암호화된 비번끼리 비교가 가능
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CustomException(UserErrorCode.WRONG_PASSWORD); // 비밀번호 틀림 에코코드 파기!
        }

        // 3. 비밀번호까지 통과했다면 토큰 발행
        return jwtTokenProvider.createToken(user.getId(), user.getEmail());
    }

    /**
     * 비밀번호 수정 기능
     * @param userId 토큰에서 추출한 로그인한 유저의 고유 ID
     */
    @Transactional
    public void updatePassword(Long userId, UserUpdatePasswordReqDto request) {
        // 1. 토큰 주인(유저)이 진짜 DB에 존재하는지 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 2. 입력한 '현재 비밀번호'가 DB에 저장된 암호화된 비밀번호와 일치하는지 검증
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new CustomException(UserErrorCode.WRONG_PASSWORD); // 현재 비밀번호 틀림 에러
        }

        // 3. 검증이 끝났다면, '새로운 비밀번호'를 다시 BCrypt로 암호화
        String encodedPassword = passwordEncoder.encode(request.newPassword());

        // 4. 엔티티의 비밀번호를 업데이트
        user.updatePassword(encodedPassword);
    }


}