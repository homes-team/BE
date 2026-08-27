package com.homes.backend.domain.verification.dto.request;

import jakarta.validation.constraints.NotNull;

public record RealtorVerificationReqDto(
        String photoUrl, // 업로드된 사진 URL(선택)

        @NotNull(message = "위도는 필수입니다.")
        Double latitude,

        @NotNull(message = "경도는 필수입니다.")
        Double longitude
) {
}
