package com.homes.backend.domain.admin.service;

import com.homes.backend.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserService userService;

    /**
     * 악성 유저 강제 탈퇴. 익명화/부수 데이터 정리 로직은 본인 탈퇴와 동일하게 UserService에 있는 걸 재사용한다.
     */
    public void withdrawUser(Long userId, String reason) {
        userService.forceWithdraw(userId, reason);
    }
}
