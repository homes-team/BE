package com.homes.backend.domain.realtor.controller;

import com.homes.backend.domain.realtor.dto.request.RealtorSignupReqDto;
import com.homes.backend.domain.realtor.dto.response.RealtorSignupResDto;
import com.homes.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "중개사(Realtor) API", description = "공인중개사 전용 회원가입을 담당하는 API")
public interface RealtorControllerDocs {

    @Operation(summary = "중개사 전용 회원가입", description = "사업자 정보와 사업자등록증/중개사무소 등록증 이미지를 입력받아 중개사 회원가입을 진행합니다. " +
            "**반드시 사전에 이메일 인증이 완료**되어야 하며, 가입 직후에는 `isVerified=false` 상태로 관리자 승인을 대기합니다.")
    ApiResponse<RealtorSignupResDto> signUpRealtor(
            @ParameterObject @ModelAttribute RealtorSignupReqDto request,
            @Parameter(description = "사업자등록증 이미지", required = true)
            @RequestPart("businessCertImage") MultipartFile businessCertImage,
            @Parameter(description = "중개사무소 등록증 이미지", required = true)
            @RequestPart("agentCertImage") MultipartFile agentCertImage,
            @Parameter(description = "프로필 사진 (선택)")
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) throws IOException;
}
