package com.homes.backend.domain.realtor.repository;

import com.homes.backend.domain.realtor.entity.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * 리뷰 등록 시 평판 점수(매너온도) 증감. read-modify-write 대신 DB 원자적 UPDATE로 처리해 동시 리뷰 간 lost update를 방지한다.
     */
    @Modifying
    @Query("UPDATE Agent a SET a.reputationScore = a.reputationScore + :delta WHERE a.id = :agentId")
    void addToReputationScore(@Param("agentId") Long agentId, @Param("delta") float delta);
}
