package com.homes.backend.domain.property.exception;

import com.homes.backend.global.exception.model.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PropertyErrorCode implements BaseErrorCode {
    PROPERTY_NOT_FOUND("PROP404_1", "존재하지 않는 매물입니다.",HttpStatus.NOT_FOUND),
    LOGIN_REQUIRED("PROP401_1", "로그인이 필요한 서비스입니다.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED_ACCESS("PROP403_1", "해당 매물을 수정/삭제할 권한이 없습니다.", HttpStatus.FORBIDDEN);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
