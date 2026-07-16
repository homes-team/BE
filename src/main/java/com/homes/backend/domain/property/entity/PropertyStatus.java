package com.homes.backend.domain.property.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PropertyStatus {
    AVAILABLE("거래가능"),
    COMPLETED("거래완료");

    private final String description;
}
