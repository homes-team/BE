package com.homes.backend.domain.bid.repository;

import com.homes.backend.domain.bid.entity.Negotiation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NegotiationRepository extends JpaRepository<Negotiation, Long> {
    List<Negotiation> findAllByBidIdOrderByCreatedAtAsc(Long bidId); //과거 -> 최신 순으로 핑퐁 기록 보여주기
}
