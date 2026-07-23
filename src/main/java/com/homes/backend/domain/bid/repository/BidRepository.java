package com.homes.backend.domain.bid.repository;

import com.homes.backend.domain.bid.entity.Bid;
import com.homes.backend.domain.bid.entity.BidStatus;
import com.homes.backend.domain.property.entity.PropertyStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BidRepository extends JpaRepository<Bid, Long> {
    @EntityGraph(attributePaths = {"agent"})
    List<Bid> findAllByPropertyIdOrderByCreatedAtDesc(Long propertyId); // 특정 매물에 달린 입찰 목록 최신순으로 조회

    /**
     * 중개사 마이페이지 통계 - 이번 달 거래(수락 확정 + 거래완료) 건수
     */
    @Query("SELECT COUNT(b) FROM Bid b " +
            "WHERE b.agent.id = :agentId AND b.status = :bidStatus " +
            "AND b.property.status = :propertyStatus " +
            "AND b.property.dealCompletedAt BETWEEN :start AND :end")
    long countCompletedDealsInRange(
            @Param("agentId") Long agentId,
            @Param("bidStatus") BidStatus bidStatus,
            @Param("propertyStatus") PropertyStatus propertyStatus,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * 중개사 마이페이지 통계 - 매물 종류별 평균 확정 수수료 (전체 기간)
     */
    @Query("SELECT b.property.propertyType AS propertyType, AVG(b.finalFee) AS averageFee " +
            "FROM Bid b " +
            "WHERE b.agent.id = :agentId AND b.finalFee IS NOT NULL " +
            "GROUP BY b.property.propertyType")
    List<AgentFeeByPropertyTypeProjection> findAverageFeeByPropertyType(@Param("agentId") Long agentId);
}
