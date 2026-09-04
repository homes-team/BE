package com.homes.backend.domain.verification.exception;

import com.homes.backend.global.exception.model.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum VerificationErrorCode implements BaseErrorCode {
    ALREADY_VERIFIED("VERIFY400_1", "이미 현장 인증이 완료된 매물입니다.", HttpStatus.BAD_REQUEST),
    EXIF_NOT_FOUND("VERIFY400_2", "사진의 메타데이터(EXIF)를 찾을 수 없습니다. (스크린샷 등 불가)", HttpStatus.BAD_REQUEST),
    EXIF_GPS_NOT_MATCH("VERIFY400_3", "사진이 촬영된 위치가 매물 반경 100m를 벗어납니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
