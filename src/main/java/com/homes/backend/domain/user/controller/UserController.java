package com.homes.backend.domain.user.controller;

import com.homes.backend.domain.user.dto.request.EmailCheckReqDto;
import com.homes.backend.domain.user.dto.request.UserCreateReqDto;
import com.homes.backend.domain.user.dto.request.UserUpdatePasswordReqDto;
import com.homes.backend.domain.user.dto.response.UserSignupResDto;
import com.homes.backend.domain.user.service.UserService;
import com.homes.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController implements UserControllerDocs {

    private final UserService userService;

    @Override
    public ApiResponse<UserSignupResDto> signUp(UserCreateReqDto request) {
        UserSignupResDto response = userService.signUp(request);
        return ApiResponse.onSuccess(response);
    }

    @Override
    public ApiResponse<Void> checkEmail(EmailCheckReqDto request) {
        userService.checkEmailDuplication(request.email());
        return ApiResponse.onSuccess();
    }

    //토큰 고민
    @Override
    @PatchMapping("/me/password")
    public ApiResponse<Void> updatePassword(@RequestBody UserUpdatePasswordReqDto request) {
        //TODO: 나중에 Spring Security 적용 후 세션이나 토큰에서 진짜 로그인한 유저 ID를 뽑아와야 함
        Long mockUserId = 1L; // 임시로 1번 유저라고 가정하고 테스트용 더미 ID 세팅

        userService.updatePassword(mockUserId, request);
        return ApiResponse.onSuccess();
    }
}