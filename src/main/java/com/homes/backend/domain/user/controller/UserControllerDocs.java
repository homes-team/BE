package com.homes.backend.domain.user.controller;

import com.homes.backend.domain.property.dto.response.PropertyListRespDto;
import com.homes.backend.domain.user.dto.request.*;
import com.homes.backend.domain.user.dto.response.UserSignupResDto;
import com.homes.backend.global.response.ApiResponse;
import com.homes.backend.global.security.TokenDto;
import com.homes.backend.global.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@Tag(name = "유저 & 인증(User) API", description = "회원가입, 로그인, 로그아웃 및 이메일 인증을 담당하는 API")
public interface UserControllerDocs {

    @Operation(summary = "이메일 중복 확인", description = "단순히 특정 이메일이 이미 DB에 가입되어 있는지 여부만 확인합니다.")
    @PostMapping("/check-email")
    ApiResponse<Void> checkEmail(@RequestBody EmailCheckReqDto request);

    @Operation(summary = "일반 회원가입", description = "새로운 유저의 회원가입을 진행합니다. **반드시 사전에 이메일 인증이 완료**되어야 승인됩니다.")
    @PostMapping("/signup")
    ApiResponse<UserSignupResDto> signUp(@RequestBody UserCreateReqDto request);

    @Operation(summary = "로그인 API", description = "이메일과 비밀번호로 로그인을 진행하고 토큰 세트를 발급합니다.")
    @PostMapping("/login")
    ApiResponse<TokenDto> login(
            @RequestBody @Valid UserLoginReqDto request
    );

    @Operation(summary = "비밀번호 변경", description = "로그인한 유저의 기존 비밀번호를 검증한 후, 새로운 비밀번호로 안전하게 암호화하여 수정합니다.")
    @PatchMapping("/me/password")
    ApiResponse<Void> updatePassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody @Valid UserUpdatePasswordReqDto request
    );

    @Operation(summary = "로그아웃", description = "현재 사용 중인 Access Token을 가로채 수명만큼 Redis 블랙리스트에 등록하여 다음 요청을 차단합니다.")
    @PostMapping("/logout")
    ApiResponse<Void> logout(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Bearer 가 포함된 Access Token을 실어주세요.", required = true)
            @RequestHeader("Authorization") String accessToken
    );

    @Operation(summary = "이메일 인증번호 발송 요청", description = "회원가입 전 이메일 중복을 검사하고, 가입 가능한 이메일이라면 6자리 인증번호를 진짜 메일함으로 쏩니다.")
    @PostMapping("/emails/verification-requests")
    ApiResponse<Void> sendVerificationCode(@RequestBody @Valid EmailCheckReqDto request);

    @Operation(summary = "이메일 인증번호 검증", description = "사용자가 메일로 받은 6자리 인증번호를 대조하여 검증합니다. 성공 시 Redis에 가입 증표를 남깁니다.")
    @PostMapping("/emails/verifications")
    ApiResponse<Void> verifyCode(@RequestBody @Valid EmailVerificationReqDto request);

    @Operation(summary = "내가 내놓은 집 확인", description = "현재 로그인한 유저가 등록한 매물 리스트를 조회합니다.")
    ApiResponse<List<PropertyListRespDto>> getMyProperties(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal
    );

    @Operation(summary = "토큰 재발급 API", description = "만료된 Access Token을 Refresh Token을 사용해 갱신합니다.")
    @PostMapping("/refresh")
    ApiResponse<TokenDto> refresh(
            @Parameter(description = "Bearer 가 포함된 Refresh Token을 실어주세요.", required = true)
            @RequestHeader("RefreshToken") String refreshToken
    );
}