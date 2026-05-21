package com.homes.backend.domain.property.controller;

import com.homes.backend.domain.property.dto.request.PropertyCreateReqDto;
import com.homes.backend.domain.property.dto.request.PropertyUpdateReqDto;
import com.homes.backend.domain.property.dto.response.PropertyDetailRespDto;
import com.homes.backend.domain.property.dto.response.PropertyListRespDto;
import com.homes.backend.domain.property.service.PropertyService;
import com.homes.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/properties")
public class PropertyController implements PropertyControllerDocs {
    private final PropertyService propertyService;

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Long> createProperty(
            @ModelAttribute PropertyCreateReqDto reqDto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws IOException {
        Long propertyId = propertyService.createProperty(reqDto, images);
        return ApiResponse.onSuccess(propertyId);
    }

    @Override
    @GetMapping
    public ApiResponse<List<PropertyListRespDto>> getAllProperties() {
        List<PropertyListRespDto> response = propertyService.getAllProperties();
        return ApiResponse.onSuccess(response);
    }

    @Override
    @GetMapping("/{propertyId}")
    public ApiResponse<PropertyDetailRespDto> getProperty(@PathVariable Long propertyId) {
        PropertyDetailRespDto response = propertyService.getProperty(propertyId);
        return ApiResponse.onSuccess(response);
    }

    @Override
    @DeleteMapping("/{propertyId}")
    public ApiResponse<Void> deleteProperty(@PathVariable Long propertyId) {
        propertyService.deleteProperty(propertyId);
        return ApiResponse.onSuccess();
    }

    @Override
    @PatchMapping(value = "/{propertyId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ApiResponse<Void> updateProperty(
            @PathVariable Long propertyId,
            @ModelAttribute PropertyUpdateReqDto reqDto,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages
    ) throws IOException { // S3 업로드 시 발생할 수 있는 에러 처리
        propertyService.updateProperty(propertyId, reqDto, newImages);
        return ApiResponse.onSuccess();
    }

}
