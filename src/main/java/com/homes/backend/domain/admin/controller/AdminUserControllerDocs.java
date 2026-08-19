package com.homes.backend.domain.admin.controller;

import com.homes.backend.domain.admin.dto.request.AdminUserWithdrawReqDto;
import com.homes.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "관리자(Admin) API", description = "관리자 전용 API")
public interface AdminUserControllerDocs {

    @Operation(summary = "악성 유저 강제 탈퇴", description = "특정 유저를 강제로 탈퇴 처리합니다. 본인 탈퇴와 동일하게 개인정보를 익명화하는 " +
            "소프트 삭제로 처리되며(신고·거래 기록은 보존), 사유를 함께 기록합니다.")
    @DeleteMapping("/{userId}")
    ApiResponse<Void> withdrawUser(
            @Parameter(description = "강제 탈퇴시킬 유저 ID") @PathVariable Long userId,
            @RequestBody @Valid AdminUserWithdrawReqDto request
    );
}
