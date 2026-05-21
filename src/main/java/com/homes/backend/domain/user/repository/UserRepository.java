package com.homes.backend.domain.user.repository;

import com.homes.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    // 이미 존재하는 이메일이면 true, 없으면 false 리턴
    boolean existsByEmail(String email);
}