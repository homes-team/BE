package com.homes.backend.domain.verification.controller;

import com.homes.backend.domain.verification.dto.request.RealtorVerificationReqDto;
import com.homes.backend.domain.verification.dto.response.VerificationStatusRespDto;
import com.homes.backend.domain.verification.entity.VerificationStatus;
import com.homes.backend.domain.verification.service.RealtorVerificationService;
import com.homes.backend.global.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/properties/{propertyId}/verifications")
public class VerificationController implements VerificationControllerDocs {
    private final RealtorVerificationService realtorVerificationService;

    /**
     * 중개사 현장 인증 요청
     */
    @PostMapping
    @Override
    public ResponseEntity<String> requestRealtorVerification(
            @PathVariable Long propertyId,
            @Valid @RequestBody RealtorVerificationReqDto reqDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal.getId();

        // 서비스가 넘겨준 상태값을 받습니다.
        VerificationStatus status = realtorVerificationService.verifyOnSite(propertyId, userId, reqDto);

        // 100m 초과로 반려된 경우 (DB 저장은 완료되었지만, 사용자에게는 실패라고 알려줌)
        if (status == VerificationStatus.REJECTED) {
            return ResponseEntity.badRequest().body("매물 위치 반경 100m를 벗어나 현장 인증이 반려되었습니다.");
        }

        // 100m 이내로 승인된 경우
        return ResponseEntity.ok("현장 인증이 성공적으로 처리되었습니다.");
    }

    /**
     * 매물 인증 상태 조회
     */
    @GetMapping
    @Override
    public ResponseEntity<VerificationStatusRespDto> getVerificationStatus(
            @PathVariable Long propertyId
    ) {
        VerificationStatusRespDto response = realtorVerificationService.getVerificationStatus(propertyId);
        return ResponseEntity.ok(response);
    }
}
