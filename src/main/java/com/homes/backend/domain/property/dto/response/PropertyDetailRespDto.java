package com.homes.backend.domain.property.dto.response;

import com.homes.backend.domain.property.entity.Property;
import com.homes.backend.domain.property.entity.PropertyImage;
import com.homes.backend.domain.property.entity.PropertyType;
import com.homes.backend.domain.property.entity.TradeType;

import java.util.List;

public record PropertyDetailRespDto(
        Long propertyId,
        List<String> imageUrls,
        String title,
        String description,
        String address,
        String detailAddress,
        TradeType tradeType,
        PropertyType propertyType,
        Long deposit,
        Long monthlyRent,
        Long maintenanceFee,
        Integer totalFloors,
        Integer currentFloor,
        Double area,
        Integer aiScore,
        Double desiredBrokerageFee,
        List<String> tags,
        Double latitude,
        Double longitude,
        int favoriteCount
) {
    public static PropertyDetailRespDto from(Property property) {
        List<String> urls = property.getImages().stream()
                .map(PropertyImage::getImageUrl)
                .toList();

        return new PropertyDetailRespDto(
                property.getId(),
                urls,
                property.getTitle(),
                property.getDescription(),
                property.getAddress(),
                property.getDetailAddress(), // 상세 주소는 상세 페이지에서만 공개
                property.getTradeType(),
                property.getPropertyType(),
                property.getDeposit(),
                property.getMonthlyRent(),
                property.getMaintenanceFee(),
                property.getTotalFloors(),
                property.getCurrentFloor(),
                property.getArea(),
                property.getAiScore(),
                property.getDesiredBrokerageFee(),
                property.getTags(),
                property.getCoordinate().getY(), // 위도(Latitude)
                property.getCoordinate().getX(),  // 경도(Longitude)
                property.getFavoriteCount()
        );
    }
}
