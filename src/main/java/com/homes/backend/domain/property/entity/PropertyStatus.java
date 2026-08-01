package com.homes.backend.domain.property.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PropertyStatus {
    AVAILABLE("거래가능"),
    MATCHED("매칭완료"), // 수수료 협상 완료(집주인이 중개사를 선택한 상태)
    COMPLETED("거래완료");

    private final String description;
}
