package com.homes.backend.domain.verification.controller;

import com.homes.backend.domain.verification.dto.request.RealtorVerificationReqDto;
import com.homes.backend.domain.verification.dto.response.VerificationStatusRespDto;
import com.homes.backend.global.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
@Tag(name = "Verification", description = "매물 인증 (집주인 서류 / 중개사 현장) API")
public interface VerificationControllerDocs {

    @Operation(summary = "중개사 현장 인증 요청", description = "중개사가 매물 현장에서 GPS 좌표와 사진을 전송하여 실매물임을 인증합니다. (오차 반경 100m 이내 자동 승인)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증 요청 및 검증 완료",
                    content = @Content(schema = @Schema(implementation = String.class, example = "현장 인증이 성공적으로 처리되었습니다."))),
            @ApiResponse(responseCode = "400", description = "잘못된 좌표 입력, 100m 반경 초과 반려, 또는 이미 승인된 매물",
                    content = @Content(schema = @Schema(implementation = String.class, example = "매물 위치 반경 100m를 벗어나 현장 인증이 반려되었습니다."))),
            @ApiResponse(responseCode = "403", description = "관리자 승인이 완료되지 않은 중개사의 요청", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 매물", content = @Content)
    })
    ResponseEntity<String> requestRealtorVerification(
            @Parameter(description = "인증할 매물의 ID", example = "1") @PathVariable Long propertyId,
            @RequestBody RealtorVerificationReqDto reqDto,
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal userPrincipal
    );

    @Operation(summary = "매물 인증 상태 조회", description = "특정 매물의 현재 인증 상태(집주인 서류 인증 여부, 중개사 현장 인증 상태)를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 매물", content = @Content)
    })
    ResponseEntity<VerificationStatusRespDto> getVerificationStatus(
            @Parameter(description = "조회할 매물의 ID", example = "1") @PathVariable Long propertyId
    );
}
