package com.homes.backend.domain.property.repository;

import com.homes.backend.domain.property.entity.Property;
import com.homes.backend.domain.property.entity.RecentView;
import com.homes.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Modifying
    @Query(value = "INSERT INTO recent_view (user_id, property_id, viewed_at, created_at, updated_at) " +
            "VALUES (:userId, :propertyId, NOW(), NOW(), NOW()) " +
            "ON CONFLICT (user_id, property_id) " +
            "DO UPDATE SET viewed_at = NOW(), updated_at = NOW()",
            nativeQuery = true)
    void upsertRecentView(@Param("userId") Long userId, @Param("propertyId") Long propertyId);
}
