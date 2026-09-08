package com.homes.backend.domain.property.controller;

import com.homes.backend.domain.property.dto.response.PresignedUrlResDto;
import com.homes.backend.global.response.ApiResponse;
import com.homes.backend.global.storage.PresignedUploadInfo;
import com.homes.backend.global.storage.S3PresignedUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/properties")
@RequiredArgsConstructor
public class PropertyUploadController implements PropertyUploadControllerDocs {

    private static final String UPLOAD_FOLDER = "properties";

    private final S3PresignedUrlService s3PresignedUrlService;

    @Override
    @GetMapping("/presigned-url")
    public ApiResponse<PresignedUrlResDto> getPresignedUrl(@RequestParam String fileName) {
        PresignedUploadInfo info = s3PresignedUrlService.issueUploadUrl(UPLOAD_FOLDER, fileName);
        return ApiResponse.onSuccess(PresignedUrlResDto.from(info));
    }
}
