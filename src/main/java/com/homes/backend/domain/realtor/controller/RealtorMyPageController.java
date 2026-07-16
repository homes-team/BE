package com.homes.backend.domain.realtor.controller;

import com.homes.backend.domain.realtor.dto.request.AgentUpdateProfileReqDto;
import com.homes.backend.domain.realtor.dto.response.AgentProfileResDto;
import com.homes.backend.domain.realtor.dto.response.NearbyPropertyResDto;
import com.homes.backend.domain.realtor.service.RealtorService;
import com.homes.backend.global.exception.CustomException;
import com.homes.backend.global.exception.GlobalErrorCode;
import com.homes.backend.global.response.ApiResponse;
import com.homes.backend.global.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/realtors")
@RequiredArgsConstructor
public class RealtorMyPageController implements RealtorMyPageControllerDocs {

    private final RealtorService realtorService;

    @Override
    @GetMapping("/me")
    public ApiResponse<AgentProfileResDto> getMyProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        validateAgent(userPrincipal);

        AgentProfileResDto response = realtorService.getMyProfile(userPrincipal.getId());
        return ApiResponse.onSuccess(response);
    }

    @Override
    @GetMapping("/me/properties/nearby")
    public ApiResponse<List<NearbyPropertyResDto>> getNearbyAvailableProperties(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        validateAgent(userPrincipal);

        List<NearbyPropertyResDto> response = realtorService.getNearbyAvailableProperties(userPrincipal.getId());
        return ApiResponse.onSuccess(response);
    }

    @Override
    @PatchMapping("/me")
    public ApiResponse<AgentProfileResDto> updateMyProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody @Valid AgentUpdateProfileReqDto request
    ) {
        validateAgent(userPrincipal);

        AgentProfileResDto response = realtorService.updateMyProfile(userPrincipal.getId(), request);
        return ApiResponse.onSuccess(response);
    }

    private void validateAgent(UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
        }
        if (!"AGENT".equals(userPrincipal.getRole())) {
            throw new CustomException(GlobalErrorCode.FORBIDDEN);
        }
    }
}
