package com.homes.backend.domain.bid.controller;

import com.homes.backend.domain.bid.dto.response.BidListRespDto;
import com.homes.backend.global.response.ApiResponse;
import com.homes.backend.global.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Tag(name = "입찰(Bid) API", description = "매물 입찰 제안서 조회 및 수수료 협상 관련 API")
public interface BidControllerDocs {

    @Operation(
            summary = "매물 입찰 제안서 목록 조회",
            description = "집주인이 자신의 매물에 달린 중개사들의 입찰 제안서 목록을 최신순으로 조회합니다."
    )
    ApiResponse<List<BidListRespDto>> getPropertyBids(
            @PathVariable Long propertyId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal
    );
}
