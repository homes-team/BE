package com.homes.backend.domain.user.dto.request;

public record UserCreateReqDto(
        String email,
        String password,
        String name,
        String nickname,
        String phone
) {}