package com.homes.backend.domain.property.dto.request;

import com.homes.backend.domain.property.entity.PropertyType;
import com.homes.backend.domain.property.entity.TradeType;
import io.swagger.v3.oas.annotations.media.Schema;

public record PropertyMapSearchReqDto(
        /**
         * 지도 영역 좌표
         */
        @Schema(description = "남서쪽 위도 (Y)", example = "37.4800")
        Double swLat,
        @Schema(description = "남서쪽 경도 (X)", example = "127.0100")
        Double swLng,
        @Schema(description = "북동쪽 위도 (Y)", example = "37.5100")
        Double neLat,
        @Schema(description = "북동쪽 경도 (X)", example = "127.0500")
        Double neLng,

        /**
         * 기본 필터
         */
        @Schema(description = "거래 종류", example = "MONTHLY_RENT")
        TradeType tradeType,
        @Schema(description = "방 종류", example = "ONE_ROOM")
        PropertyType propertyType,

        /**
         * 가격대 필터
         */
        @Schema(description = "최소 보증금(만원)", example = "500")
        Integer minDeposit, // 최소 보증금
        @Schema(description = "최대 보증금(만원)", example = "2000")
        Integer maxDeposit, // 최대 보증금
        @Schema(description = "최소 월세(만원)", example = "40")
        Integer minMonthlyRent, // 최소 월세
        @Schema(description = "최대 월세(만원)", example = "100")
        Integer maxMonthlyRent, // 최대 월세

        /**
         * 추가 필터 (예: 주차가능, 엘리베이터)
         */
        @Schema(description = "태그 검색 키워드", example = "신축")
        String keyword // tags 컬럼에서 검색할 키워드
) {
}
