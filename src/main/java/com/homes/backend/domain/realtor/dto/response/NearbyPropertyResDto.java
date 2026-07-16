package com.homes.backend.domain.realtor.dto.response;

import com.homes.backend.domain.property.entity.Property;
import com.homes.backend.domain.property.entity.TradeType;

public record NearbyPropertyResDto(
        Long propertyId,
        String address,
        String detailAddress,
        TradeType tradeType,
        Long deposit,
        Long monthlyRent,
        Double desiredBrokerageFee,
        Double distanceInMeters
) {
    public static NearbyPropertyResDto from(Property property, double distanceInMeters) {
        return new NearbyPropertyResDto(
                property.getId(),
                property.getAddress(),
                property.getDetailAddress(),
                property.getTradeType(),
                property.getDeposit(),
                property.getMonthlyRent(),
                property.getDesiredBrokerageFee(),
                distanceInMeters
        );
    }
}
