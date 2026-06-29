package com.homes.backend.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record OAuthLoginReqDto(
        @Schema(description = "구글 인증 코드", example = "4/0AfgeXv...")
        String authorizationCode
) {}