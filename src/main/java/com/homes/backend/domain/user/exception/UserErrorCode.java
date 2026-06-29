package com.homes.backend.domain.user.exception;

import com.homes.backend.global.exception.model.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

    DUPLICATE_EMAIL("USER400_1", "이미 사용 중인 이메일입니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_NICKNAME("USER400_2", "이미 사용 중인 닉네임입니다.", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("USER400_3", "유저를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),
    WRONG_PASSWORD("USER400_4", "비밀번호가 틀렸습니다.", HttpStatus.BAD_REQUEST),
    VERIFICATION_CODE_EXPIRED("USER400_5", "인증번호가 만료되었거나 없습니다.", HttpStatus.BAD_REQUEST),
    WRONG_VERIFICATION_CODE("USER400_6", "인증번호가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_VERIFIED("USER400_7","이메일 인증이 완료되지 않은 사용자입니다.", HttpStatus.BAD_REQUEST),
    INVALID_REFRESH_TOKEN("USER400_8", "유효하지 않거나 만료된 Refresh Token입니다.", HttpStatus.BAD_REQUEST),
    INVALID_GOOGLE_CODE("USER400_9", "유효하지 않거나 구글 통신 장애가 있습니다.",HttpStatus.BAD_REQUEST );
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}