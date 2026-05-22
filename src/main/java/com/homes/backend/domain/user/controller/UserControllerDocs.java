package com.homes.backend.domain.user.controller;

import com.homes.backend.domain.user.dto.request.EmailCheckReqDto;
import com.homes.backend.domain.user.dto.request.UserCreateReqDto;
import com.homes.backend.domain.user.dto.request.UserUpdatePasswordReqDto;
import com.homes.backend.domain.user.dto.response.UserSignupResDto;
import com.homes.backend.global.response.ApiResponse;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;

//@RequestMapping 같은 공통 주소 매핑은 인터페이스에 둡니다.
public interface UserControllerDocs {

    // (나중에 여기에 @Operation 같은 지저분한 스웨거 태그들이 붙음)
    @PostMapping("/check-email")
    ApiResponse<Void> checkEmail(@RequestBody EmailCheckReqDto request);

    @PostMapping("/signup")
    ApiResponse<UserSignupResDto> signUp(@RequestBody UserCreateReqDto request);

    @PatchMapping("/me/password")
    ApiResponse<Void> updatePassword(
            // 원래는 인증 객체가 들어가지만, 우선 컴파일을 위해 DTO만 세팅하거나 임시 파라미터를 둡니다.
            @RequestBody UserUpdatePasswordReqDto request
    );


}