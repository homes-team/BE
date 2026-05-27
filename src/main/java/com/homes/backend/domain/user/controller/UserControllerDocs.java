package com.homes.backend.domain.user.controller;

import com.homes.backend.domain.property.dto.response.PropertyListRespDto;
import com.homes.backend.domain.user.dto.request.*;
import com.homes.backend.domain.user.dto.response.UserSignupResDto;
import com.homes.backend.global.response.ApiResponse;
import com.homes.backend.global.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;


public interface UserControllerDocs {

    @PostMapping("/check-email")
    ApiResponse<Void> checkEmail(@RequestBody EmailCheckReqDto request);

    @PostMapping("/signup")
    ApiResponse<UserSignupResDto> signUp(@RequestBody UserCreateReqDto request);

    @PostMapping("/login")
    ApiResponse<String> login(@RequestBody UserLoginReqDto request);

    @PatchMapping("/me/password")
    ApiResponse<Void> updatePassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody @Valid UserUpdatePasswordReqDto request
    );

    @PostMapping("/logout")
    ApiResponse<Void> logout(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestHeader("Authorization") String accessToken
    );

    @PostMapping("/emails/verification-requests")
    ApiResponse<Void> sendVerificationCode(@RequestBody @Valid EmailCheckReqDto request);

    @PostMapping("/emails/verifications")
    ApiResponse<Void> verifyCode(@RequestBody @Valid EmailVerificationReqDto request);

    @Operation(summary = "내가 내놓은 집 확인", description = "현재 로그인한 유저가 등록한 매물 리스트를 조회합니다.")
    ApiResponse<List<PropertyListRespDto>> getMyProperties(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal
    );
}