package com.homes.backend.domain.realtor.repository;

import com.homes.backend.domain.realtor.entity.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgentRepository extends JpaRepository<Agent, Long> {
    boolean existsByBusinessNum(String businessNum);

    /**
     * 회원 탈퇴 시 중개사 프로필 삭제
     */
    void deleteByUserId(Long userId);

    /**
     * 로그인한 유저의 중개사 프로필 조회
     */
    Optional<Agent> findByUserId(Long userId);
}
