package com.homes.backend.domain.property.controller;

import com.homes.backend.domain.property.dto.response.PropertyListRespDto;
import com.homes.backend.domain.property.service.PropertyRecommendationService;
import com.homes.backend.global.exception.CustomException;
import com.homes.backend.global.exception.GlobalErrorCode;
import com.homes.backend.global.response.ApiResponse;
import com.homes.backend.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/properties")
public class PropertyRecommendationController implements PropertyRecommendationControllerDocs{
    private final PropertyRecommendationService propertyRecommendationService;

    @Override
    @GetMapping("/recommendations")
    public ApiResponse<List<PropertyListRespDto>> getHybridRecommendations(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        if (userPrincipal == null) {
            throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
        }

        List<PropertyListRespDto> recommendations = propertyRecommendationService.getRecommendations(userPrincipal.getId());
        return ApiResponse.onSuccess(recommendations);
    }
}
