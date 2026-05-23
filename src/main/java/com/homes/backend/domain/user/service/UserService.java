package com.homes.backend.domain.user.service;

import com.homes.backend.domain.user.dto.request.EmailVerificationReqDto;
import com.homes.backend.domain.user.dto.request.UserCreateReqDto;
import com.homes.backend.domain.user.dto.request.UserLoginReqDto;
import com.homes.backend.domain.user.dto.request.UserUpdatePasswordReqDto;
import com.homes.backend.domain.user.dto.response.UserSignupResDto;
import com.homes.backend.domain.user.entity.User;
import com.homes.backend.domain.user.repository.UserRepository;
import com.homes.backend.domain.user.exception.UserErrorCode;
import com.homes.backend.global.exception.CustomException;
import com.homes.backend.global.security.JwtTokenProvider;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JavaMailSender mailSender;
    private static final long AUTH_CODE_EXPIRATION = 300L; // 인증번호 유효시간: 5분 (300초)

    public void checkEmailDuplication(String email) {
        if (userRepository.existsByEmail(email)) {
            // 중복 시 에러 코드 실어 핸들러가 가로챔
            throw new CustomException(UserErrorCode.DUPLICATE_EMAIL);
        }
    }

    @Transactional
    public UserSignupResDto signUp(UserCreateReqDto request) {
        // 1. 이메일 DB 중복 검사
        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException(UserErrorCode.DUPLICATE_EMAIL);
        }

        String isVerified = (String) redisTemplate.opsForValue().get("AUTH_SUCCESS:" + request.email());

        if (isVerified == null || !isVerified.equals("TRUE")) {
            throw new CustomException(UserErrorCode.EMAIL_NOT_VERIFIED);
        }

        // 2. 엔티티 생성 및 저장
        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();

        User savedUser = userRepository.save(user);

        // 가입이 완벽히 끝났으니 Redis에 있던 합격증은 깔끔하게 파기
        redisTemplate.delete("AUTH_SUCCESS:" + request.email());

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

    //Redis를 활용한 로그아웃 (블랙리스트 등록)
    @Transactional
    public void logout(String accessToken) {
        // 1. 토큰이 무조건 "Bearer "로 시작하니까 알맹이만 쏙 빼내기
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }

        // 2. 이 토큰이 앞으로 얼마나 더 살아있을 수 있는지 남은 유효시간(ms) 계산
        Long expiration = jwtTokenProvider.getExpiration(accessToken);

        // 3. 만약 이미 만료된 토큰이 아니라면, 남은 시간만큼만 Redis에 "BLACK:" 접두사를 붙여 저장!
        if (expiration > 0) {
            redisTemplate.opsForValue().set(
                    "BLACK:" + accessToken,
                    "logout",
                    expiration,
                    java.util.concurrent.TimeUnit.MILLISECONDS
            );
        }
    }

    //1. 이메일 인증번호 요청 (+ 중복 검사)
    @Transactional
    public void sendVerificationCode(String email) {
        // DB 중복 검사
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(UserErrorCode.DUPLICATE_EMAIL);
        }

        // 6자리 랜덤 인증번호 생성
        String verificationCode = String.valueOf((int)(Math.random() * 899999) + 100000);

        // Redis에 5분간 저장
        redisTemplate.opsForValue().set(
                "AUTH:" + email,
                verificationCode,
                java.util.concurrent.TimeUnit.SECONDS.toMillis(AUTH_CODE_EXPIRATION),
                java.util.concurrent.TimeUnit.MILLISECONDS
        );

        try {
            MimeMessage message = mailSender.createMimeMessage();

            // 생성자 단계에서 명확하게 "UTF-8" 인코딩을 강제로 선언해버립니다.
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 로그인 계정 주소와 메일 보내는 명의를 일치시키는 설정
            helper.setFrom("homescompanyduksung@gmail.com");

            // [조치] 제목에서 구글이 오해할만한 대괄호([])를 빼고 직관적으로 적습니다.
            helper.setSubject("Homes Verification Code");

            helper.setTo(email); // 받는 사람 이메일 (스웨거에 치는 주소로 배달됨!)

            // 메일 본문 내용 (가장 직관적이고 안전한 형태로 정돈)
            String content = "<div style='margin:20px;'>" +
                    "<h3>Homes 회원가입 인증번호 안내</h3>" +
                    "<p>아래 인증번호를 화면에 입력해주세요.</p>" +
                    "<h2 style='color:#304FFE;'>" + verificationCode + "</h2>" +
                    "<p>본 인증번호는 5분간 유효합니다.</p>" +
                    "</div>";

            helper.setText(content, true); // HTML 형식 지정

            // 우체부 발송!
            mailSender.send(message);

            System.out.println("[성공] 진짜 사용자의 이메일함으로 메일 전송 완료!");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("이메일 발송에 실패했습니다.");
        }
    }

    //2. 이메일 인증번호 검증
    public void verifyCode(EmailVerificationReqDto request) {
        //Redis 금고에서 이메일에 해당하는 인증번호 꺼내오기
        String savedCode = (String) redisTemplate.opsForValue().get("AUTH:" + request.email());

        //인증번호가 만료되었거나 없는 경우
        if (savedCode == null) {
            throw new CustomException(UserErrorCode.VERIFICATION_CODE_EXPIRED); // 에러코드 정의 필요!
        }

        //사용자가 입력한 번호와 금고의 번호가 일치하는지 대조
        if (!savedCode.equals(request.code())) {
            throw new CustomException(UserErrorCode.WRONG_VERIFICATION_CODE); // 에러코드 정의 필요!
        }

        //[선택] 검증 완료 시, 회원가입 때 "이 사람 인증된 사람 맞아?"라고 최종 확인할 수 있도록
        //Redis에 "AUTH_SUCCESS:" 상태를 5분간 남겨두면 가입 API 보안을 더 올릴 수 있음.
        redisTemplate.opsForValue().set("AUTH_SUCCESS:" + request.email(), "TRUE", 5, java.util.concurrent.TimeUnit.MINUTES);

        // 검증 성공했으니 기존 인증 데이터는 Redis에서 삭제 처리
        redisTemplate.delete("AUTH:" + request.email());
    }

}