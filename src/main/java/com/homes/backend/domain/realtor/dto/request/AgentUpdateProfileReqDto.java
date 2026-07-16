package com.homes.backend.domain.realtor.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "중개사 마이페이지 회원정보 수정 요청 DTO. 값을 보내지 않은 필드는 기존 값이 유지됩니다.")
public record AgentUpdateProfileReqDto(
        @Schema(description = "중개사무소 이름", example = "홈즈공인중개사사무소")
        @Size(min = 1, max = 50, message = "중개사무소명은 1자 이상 50자 이하로 입력해주세요.")
        String officeName,

        @Schema(description = "중개사무소 주소", example = "서울 강남구 역삼동 123-45")
        @Size(min = 1, max = 255, message = "중개사무소 주소는 1자 이상 255자 이하로 입력해주세요.")
        String officeAddress,

        @Schema(description = "중개사무소 위도 (지도 API에서 추출)", example = "37.4979")
        Double officeLatitude,

        @Schema(description = "중개사무소 경도 (지도 API에서 추출)", example = "127.0276")
        Double officeLongitude
) {}
