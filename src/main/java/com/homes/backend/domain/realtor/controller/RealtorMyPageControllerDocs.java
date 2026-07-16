package com.homes.backend.domain.realtor.controller;

import com.homes.backend.domain.realtor.dto.request.AgentUpdateProfileReqDto;
import com.homes.backend.domain.realtor.dto.response.AgentProfileResDto;
import com.homes.backend.domain.realtor.dto.response.NearbyPropertyResDto;
import com.homes.backend.global.response.ApiResponse;
import com.homes.backend.global.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "중개사(Realtor) API", description = "공인중개사 마이페이지를 담당하는 API")
public interface RealtorMyPageControllerDocs {

    @Operation(summary = "중개사 마이페이지 - 내 프로필 조회", description = "로그인한 중개사 본인의 프로필(사무소명, 주소, 사업자등록번호, 인증 여부 등)을 조회합니다.")
    @GetMapping("/me")
    ApiResponse<AgentProfileResDto> getMyProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal
    );

    @Operation(summary = "중개사 마이페이지 - 사무소 기준 거리순 신규 매물 조회", description = "중개사무소 위치를 기준으로, 거래가능 상태인 매물을 가까운 순으로 정렬해 조회합니다. " +
            "회원가입 시 사무소 주소/좌표를 등록하지 않은 경우 조회할 수 없습니다.")
    @GetMapping("/me/properties/nearby")
    ApiResponse<List<NearbyPropertyResDto>> getNearbyAvailableProperties(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal
    );

    @Operation(summary = "중개사 마이페이지 - 내 프로필 수정", description = "중개사무소 이름/주소/좌표를 수정합니다. " +
            "값을 보내지 않은 필드는 기존 값이 유지됩니다(부분 수정). " +
            "사업자등록번호, 인증 서류, 인증 여부는 관리자 승인과 직결되므로 이 API로는 수정할 수 없습니다.")
    @PatchMapping("/me")
    ApiResponse<AgentProfileResDto> updateMyProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody @Valid AgentUpdateProfileReqDto request
    );
}
