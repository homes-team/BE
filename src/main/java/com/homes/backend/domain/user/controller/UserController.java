package com.homes.backend.domain.user.controller;

import com.homes.backend.domain.property.dto.response.PropertyListRespDto;
import com.homes.backend.domain.property.dto.response.ReportListRespDto;
import com.homes.backend.domain.property.service.PropertyReportService;
import com.homes.backend.domain.property.service.PropertyService;
import com.homes.backend.domain.user.dto.request.*;
import com.homes.backend.domain.user.dto.response.UserSignupResDto;
import com.homes.backend.domain.user.service.UserService;
import com.homes.backend.global.exception.CustomException;
import com.homes.backend.global.exception.GlobalErrorCode;
import com.homes.backend.global.response.ApiResponse;
import com.homes.backend.global.security.TokenDto;
import com.homes.backend.global.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController implements UserControllerDocs {

    private final UserService userService;
    private final PropertyService propertyService;
    private final PropertyReportService propertyReportService;

    @Override
    @PostMapping("/signup")
    public ApiResponse<UserSignupResDto> signUp(@RequestBody @Valid UserCreateReqDto request) {
        UserSignupResDto response = userService.signUp(request);
        return ApiResponse.onSuccess(response);
    }

    @Override
    @PostMapping("/check-email")
    public ApiResponse<Void> checkEmail(EmailCheckReqDto request) {
        userService.checkEmailDuplication(request.email());
        return ApiResponse.onSuccess();
    }

    @Override
    @PostMapping("/login")
    public ApiResponse<TokenDto> login(
            @RequestBody @Valid UserLoginReqDto request
    ) {
        // 이제 TokenDto를 리턴
        TokenDto tokenDto = userService.login(request);
        return ApiResponse.onSuccess(tokenDto);
    }

    @Override
    @PatchMapping("/me/password")
    public ApiResponse<Void> updatePassword(
            // 로그인한 사람의 UserPrincipal 상자를 꺼내줌
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody @Valid UserUpdatePasswordReqDto request
    ) {
        // 토큰 주인의 ID가 안전하게 전달
        userService.updatePassword(userPrincipal.getId(), request);
        return ApiResponse.onSuccess(null);
    }

    @Override
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestHeader("Authorization") String accessToken
    ) {
        userService.logout(accessToken);
        return ApiResponse.onSuccess(null);
    }

    @Override
    @PostMapping("/emails/verification-requests")
    public ApiResponse<Void> sendVerificationCode(@RequestBody @Valid EmailCheckReqDto request) {
        userService.sendVerificationCode(request.email());
        return ApiResponse.onSuccess(null);
    }

    @Override
    @PostMapping("/emails/verifications")
    public ApiResponse<Void> verifyCode(@RequestBody @Valid EmailVerificationReqDto request) {
        userService.verifyCode(request);
        return ApiResponse.onSuccess(null);
    }

    @Override
    @GetMapping("/me/properties")
    public ApiResponse<List<PropertyListRespDto>> getMyProperties(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        if (userPrincipal == null) {
            throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
        }

        List<PropertyListRespDto> response = propertyService.getMyProperties(userPrincipal.getId());

        return ApiResponse.onSuccess(response);
    }

    @Override
    @PostMapping("/refresh")
    public ApiResponse<TokenDto> refresh(
            @RequestHeader("RefreshToken") String refreshToken
    ) {
        TokenDto tokenDto = userService.refresh(refreshToken);
        return ApiResponse.onSuccess(tokenDto);
    }

    @Override
    @GetMapping("/me/favorites")
    public ApiResponse<List<PropertyListRespDto>> getMyFavoriteProperties(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        if (userPrincipal == null) {
            throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
        }

        List<PropertyListRespDto> response = propertyService.getMyFavoriteProperties(userPrincipal.getId());
        return ApiResponse.onSuccess(response);
    }
    @Override
    @PostMapping("/oauth/google")
    public ApiResponse<TokenDto> googleLogin(@RequestBody @Valid OAuthLoginReqDto reqDto) {
        TokenDto tokenDto = userService.googleLogin(reqDto.authorizationCode());
        return ApiResponse.onSuccess(tokenDto);
    }

    @Override
    @GetMapping("/me/reports")
    public ApiResponse<List<ReportListRespDto>> getMyReports(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ){
        if(userPrincipal == null){
            throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
        }
        List<ReportListRespDto> response= propertyReportService.getMyReports(userPrincipal.getId());
        return ApiResponse.onSuccess(response);
    }
}