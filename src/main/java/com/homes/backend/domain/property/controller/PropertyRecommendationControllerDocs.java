package com.homes.backend.domain.property.controller;

import com.homes.backend.domain.property.dto.response.PropertyListRespDto;
import com.homes.backend.global.response.ApiResponse;
import com.homes.backend.global.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@Tag(name = "매물 추천 API", description = "AI, 찜하기, 최근 본 방 기반 추천 매물 조회 API")
public interface PropertyRecommendationControllerDocs {
    @Operation(
            summary = "하이브리드 맞춤 추천 매물 조회",
            description = "AI 점수(50%), 찜하기(45%), 최근 본 방 보너스(5%)를 결합하여 로그인한 유저에게 상위 10개의 맞춤 매물을 추천합니다."
    )
    ApiResponse<List<PropertyListRespDto>> getHybridRecommendations(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal
    );
}
