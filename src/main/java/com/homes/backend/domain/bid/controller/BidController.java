package com.homes.backend.domain.bid.controller;

import com.homes.backend.domain.bid.dto.response.BidListRespDto;
import com.homes.backend.domain.bid.service.BidService;
import com.homes.backend.global.exception.CustomException;
import com.homes.backend.global.exception.GlobalErrorCode;
import com.homes.backend.global.response.ApiResponse;
import com.homes.backend.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/properties/{propertyId}/bids")
public class BidController implements BidControllerDocs{
    private final BidService bidService;

    @Override
    @GetMapping
    public ApiResponse<List<BidListRespDto>> getPropertyBids(
            @PathVariable Long propertyId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        if (userPrincipal == null) { // 비로그인 사용자 차단
            throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
        }

        List<BidListRespDto> response = bidService.getPropertyBids(propertyId, userPrincipal.getId());
        return ApiResponse.onSuccess(response);
    }
}
