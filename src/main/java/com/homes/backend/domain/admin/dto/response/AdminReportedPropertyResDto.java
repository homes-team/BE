package com.homes.backend.domain.admin.dto.response;

import com.homes.backend.domain.property.entity.Property;

public record AdminReportedPropertyResDto(
        Long propertyId,
        String title,
        String address,
        int reportCount,
        boolean isSuspicious
) {
    public static AdminReportedPropertyResDto from(Property property) {
        return new AdminReportedPropertyResDto(
                property.getId(),
                property.getTitle(),
                property.getAddress(),
                property.getReportCount(),
                property.isSuspicious()
        );
    }
}
