package com.homes.backend.domain.realtor.controller;

import com.homes.backend.domain.realtor.dto.request.RealtorSignupReqDto;
import com.homes.backend.domain.realtor.dto.response.RealtorSignupResDto;
import com.homes.backend.domain.realtor.service.RealtorService;
import com.homes.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class RealtorController implements RealtorControllerDocs {

    private final RealtorService realtorService;

    @Override
    @PostMapping(value = "/realtors", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<RealtorSignupResDto> signUpRealtor(
            @ModelAttribute @Valid RealtorSignupReqDto request,
            @RequestPart("businessCertImage") MultipartFile businessCertImage,
            @RequestPart("agentCertImage") MultipartFile agentCertImage,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) throws IOException {
        RealtorSignupResDto response = realtorService.signUp(request, businessCertImage, agentCertImage, profileImage);
        return ApiResponse.onSuccess(response);
    }
}
