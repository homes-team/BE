package com.homes.backend.domain.bid.dto.request;

public record BidCreateReqDto(
        Double proposedFee, // 중개사가 역으로 제안하는 수수료(%)
        String content      // 중개사 어필 메시지
) {
}
