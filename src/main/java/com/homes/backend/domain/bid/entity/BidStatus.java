package com.homes.backend.domain.bid.entity;

public enum BidStatus {
    PENDING,    // 대기
    ACCEPTED,   // 수락
    REJECTED,   // 거절
    CANCELLED   // 수락됐다가 매칭 자체가 취소됨 (REJECTED와 구분: 애초에 선택 안 된 것과 선택됐다가 틀어진 것은 의미가 다름)
}
