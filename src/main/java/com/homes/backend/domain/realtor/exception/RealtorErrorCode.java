package com.homes.backend.domain.realtor.exception;

import com.homes.backend.global.exception.model.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RealtorErrorCode implements BaseErrorCode {

    DUPLICATE_BUSINESS_NUM("REALTOR400_1", "이미 등록된 사업자등록번호입니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
