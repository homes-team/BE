package com.homes.backend.domain.property.repository;

import com.homes.backend.domain.property.entity.Property;
import com.homes.backend.domain.property.entity.RecentView;
import com.homes.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecentViewRepository extends JpaRepository<RecentView, Long> {

    /**
     * 기존 시청 기록 찾기 (Upsert 검사용)
     */
    Optional<RecentView> findByUserAndProperty(User user, Property property);

    /**
     * 내가 최근 본 방 목록 최신순 조회(Top 20개 조회)
     */
    List<RecentView> findTop20ByUserIdOrderByViewedAtDesc(Long userId);
}
