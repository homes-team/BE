package com.homes.backend.domain.property.controller;

import com.homes.backend.domain.property.dto.request.PropertyCreateReqDto;
import com.homes.backend.domain.property.dto.request.PropertyUpdateReqDto;
import com.homes.backend.domain.property.dto.response.PropertyDetailRespDto;
import com.homes.backend.domain.property.dto.response.PropertyListRespDto;
import com.homes.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "매물(Property) API", description = "매물 등록, 상세 조회, 삭제를 담당하는 API")
public interface PropertyControllerDocs {
    @Operation(summary = "매물 등록", description = "글 데이터(JSON)와 사진 파일들(Images)을 한 번에 등록합니다.")
    ApiResponse<Long> createProperty(
            @ParameterObject @ModelAttribute PropertyCreateReqDto reqDto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws IOException;

    @Operation(summary = "전체 매물 리스트 조회", description = "카드형 UI에 들어갈 매물들의 간단한 정보 리스트를 조회합니다.")
    ApiResponse<List<PropertyListRespDto>> getAllProperties();

    @Operation(summary = "매물 상세 조회", description = "매물 ID를 통해 특정 매물의 상세 정보를 조회합니다.")
    ApiResponse<PropertyDetailRespDto> getProperty(@PathVariable Long propertyId);

    @Operation(summary = "매물 삭제", description = "매물 ID를 통해 특정 매물을 삭제합니다.")
    ApiResponse<Void> deleteProperty(@PathVariable Long propertyId);

    @Operation(summary = "매물 수정", description = "글 데이터(JSON)와 새로운 사진 파일들을 수정합니다.")
    ApiResponse<Void> updateProperty(
            @PathVariable Long propertyId,
            @ParameterObject @ModelAttribute PropertyUpdateReqDto reqDto,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages
    ) throws IOException;
}

