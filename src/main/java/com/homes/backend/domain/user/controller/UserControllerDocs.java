package com.homes.backend.domain.user.controller;

import com.homes.backend.domain.user.dto.request.*;
import com.homes.backend.domain.user.dto.response.UserSignupResDto;
import com.homes.backend.global.response.ApiResponse;
import com.homes.backend.global.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestHeader;


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

}