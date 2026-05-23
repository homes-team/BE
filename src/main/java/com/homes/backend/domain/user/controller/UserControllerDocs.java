package com.homes.backend.domain.user.controller;

import com.homes.backend.domain.user.dto.request.EmailCheckReqDto;
import com.homes.backend.domain.user.dto.request.UserCreateReqDto;
import com.homes.backend.domain.user.dto.request.UserLoginReqDto;
import com.homes.backend.domain.user.dto.request.UserUpdatePasswordReqDto;
import com.homes.backend.domain.user.dto.response.UserSignupResDto;
import com.homes.backend.global.response.ApiResponse;
import com.homes.backend.global.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


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

}