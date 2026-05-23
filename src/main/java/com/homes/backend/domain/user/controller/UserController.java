package com.homes.backend.domain.user.controller;

import com.homes.backend.domain.user.dto.request.EmailCheckReqDto;
import com.homes.backend.domain.user.dto.request.UserCreateReqDto;
import com.homes.backend.domain.user.dto.request.UserLoginReqDto;
import com.homes.backend.domain.user.dto.request.UserUpdatePasswordReqDto;
import com.homes.backend.domain.user.dto.response.UserSignupResDto;
import com.homes.backend.domain.user.service.UserService;
import com.homes.backend.global.response.ApiResponse;
import com.homes.backend.global.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController implements UserControllerDocs {

    private final UserService userService;

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
    public ApiResponse<String> login(@RequestBody UserLoginReqDto request) {
        // 로그인이 성공하면 토큰 문자열이 튀어나옴.
        String token = userService.login(request);
        // 성공 상자에 토큰을 담아서 프론트엔드에게 리턴
        return ApiResponse.onSuccess(token);
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

}