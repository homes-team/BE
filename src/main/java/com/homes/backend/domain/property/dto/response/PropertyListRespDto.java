package com.homes.backend.domain.property.dto.response;

import com.homes.backend.domain.property.entity.Property;
import com.homes.backend.domain.property.entity.PropertyImage;
import com.homes.backend.domain.property.entity.PropertyType;
import com.homes.backend.domain.property.entity.TradeType;

import java.time.LocalDateTime;
import java.util.List;

public record PropertyListRespDto(
        Long propertyId,
        String thumbnailUrl,
        PropertyType propertyType,
        TradeType tradeType,
        Long deposit,
        Long monthlyRent,
        Integer totalFloors,
        Integer currentFloor,
        Double area,
        String description,
        LocalDateTime createdAt,
        Integer aiScore,
        List<String> tags,
        Integer favoriteCount,
        boolean isSuspicious
) {
    public static PropertyListRespDto from(Property property) {
        /**
         * 여러 사진 중 대표 이미지(isThumbnail=true)만 찾아서 URL 추출
          */
        String thumbnail = property.getImages().stream()
                .filter(PropertyImage::getIsThumbnail)
                .map(PropertyImage::getImageUrl)
                .findFirst()
                .orElse(null); // 사진이 한 장도 없으면 null 반환

        return new PropertyListRespDto(
                property.getId(),
                thumbnail,
                property.getPropertyType(),
                property.getTradeType(),
                property.getDeposit(),
                property.getMonthlyRent(),
                property.getTotalFloors(),
                property.getCurrentFloor(),
                property.getArea(),
                property.getDescription(),
                property.getCreatedAt(),
                property.getAiScore(),
                property.getTags(),
                property.getFavoriteCount(),
                property.isSuspicious()
        );
    }
}
