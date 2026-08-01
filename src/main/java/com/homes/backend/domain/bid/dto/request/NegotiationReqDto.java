package com.homes.backend.domain.bid.dto.request;

public record NegotiationReqDto (
        Double suggestedFee, // 새로 제안하는 수수료
        String message
){

}
