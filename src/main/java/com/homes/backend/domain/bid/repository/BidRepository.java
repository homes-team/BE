package com.homes.backend.domain.bid.repository;

import com.homes.backend.domain.bid.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findAllByPropertyIdOrderByCreatedAtDesc(Long propertyId); // 특정 매물에 달린 입찰 목록 최신순으로 조회
}
