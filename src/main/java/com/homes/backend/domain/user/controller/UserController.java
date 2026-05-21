package com.homes.backend.domain.user.controller;

import com.homes.backend.domain.user.dto.request.EmailCheckReqDto;
import com.homes.backend.domain.user.dto.request.UserCreateReqDto;
import com.homes.backend.domain.user.dto.response.UserSignupResDto;
import com.homes.backend.domain.user.service.UserService;
import com.homes.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
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
}