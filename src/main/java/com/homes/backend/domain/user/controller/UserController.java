package com.homes.backend.domain.user.controller;

import com.homes.backend.domain.user.dto.request.EmailCheckReqDto;
import com.homes.backend.domain.user.dto.request.UserCreateReqDto;
import com.homes.backend.domain.user.dto.request.UserLoginReqDto;
import com.homes.backend.domain.user.dto.response.UserSignupResDto;
import com.homes.backend.domain.user.service.UserService;
import com.homes.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @Override
    public ApiResponse<String> login(@RequestBody UserLoginReqDto request) {
        // 로그인이 성공하면 토큰 문자열이 튀어나옴.
        String token = userService.login(request);
        // 성공 상자에 토큰을 담아서 프론트엔드에게 리턴
        return ApiResponse.onSuccess(token);
    }

}