package com.homes.backend.domain.verification.exception;

import com.homes.backend.global.exception.model.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum VerificationErrorCode implements BaseErrorCode {
    ALREADY_VERIFIED("VERIFY400_1", "이미 현장 인증이 완료된 매물입니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
