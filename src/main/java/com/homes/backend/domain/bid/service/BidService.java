package com.homes.backend.domain.bid.service;

import com.homes.backend.domain.bid.dto.request.BidCreateReqDto;
import com.homes.backend.domain.bid.dto.request.NegotiationReqDto;
import com.homes.backend.domain.bid.dto.response.BidListRespDto;
import com.homes.backend.domain.bid.dto.response.NegotiationListResDto;
import com.homes.backend.domain.bid.entity.Bid;
import com.homes.backend.domain.bid.entity.BidStatus;
import com.homes.backend.domain.bid.entity.Negotiation;
import com.homes.backend.domain.bid.exception.BidErrorCode;
import com.homes.backend.domain.bid.repository.BidRepository;
import com.homes.backend.domain.bid.repository.NegotiationRepository;
import com.homes.backend.domain.property.entity.Property;
import com.homes.backend.domain.property.entity.PropertyStatus;
import com.homes.backend.domain.property.exception.PropertyErrorCode;
import com.homes.backend.domain.property.repository.PropertyRepository;
import com.homes.backend.domain.realtor.entity.Agent;
import com.homes.backend.domain.realtor.repository.AgentRepository;
import com.homes.backend.global.exception.CustomException;
import com.homes.backend.global.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BidService {

    private final BidRepository bidRepository;
    private final PropertyRepository propertyRepository;
    private final AgentRepository agentRepository;
    private final NegotiationRepository negotiationRepository;

    /**
     * 중개사가 수수료 역제안 제시
     */
    @Transactional
    public void createBid(Long propertyId, BidCreateReqDto reqDto, Long userId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new CustomException(PropertyErrorCode.PROPERTY_NOT_FOUND));

        // 입찰하는 중개사(Agent) 조회
        Agent agent = agentRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND));

        // 이미 해당 매물에 입찰서를 제출했는지 검사
        if (bidRepository.existsByPropertyIdAndAgentId(propertyId, agent.getId())) {
            throw new CustomException(BidErrorCode.ALREADY_BIDDED);
        }

        Bid bid = Bid.builder()
                .proposedFee(reqDto.proposedFee())
                .content(reqDto.content())
                .agent(agent)
                .property(property)
                .build();

        bidRepository.save(bid);
    }

    /**
     * 특정 매물의 입찰 제안서 리스트 조회 (집주인 전용)
     */
    public List<BidListRespDto> getPropertyBids(Long propertyId, Long userId) {
        Property property = getPropertyAndValidateOwnership(propertyId, userId);

        return bidRepository.findAllByPropertyIdOrderByCreatedAtDesc(propertyId).stream()
                .map(BidListRespDto::from)
                .toList();
    }

    /**
     * 역제안 내역 조회
     */
    public List<NegotiationListResDto> getNegotiationList(Long propertyId, Long bidId, Long userId, String role) {
        validateAccessRight(propertyId, bidId, userId, role);

        return negotiationRepository.findAllByBidIdOrderByCreatedAtAsc(bidId).stream()
                .map(NegotiationListResDto::from)
                .toList();
    }

    /**
     * 수수료 역제안 전송
     */
    @Transactional
    public void createNegotiation(Long propertyId, Long bidId, NegotiationReqDto reqDto, Long userId, String role) {
        Bid bid = validateAccessRight(propertyId, bidId, userId, role);

        // 이미 매칭 완료된 제안서면 추가 협상 불가 처리
        if (bid.getStatus() == BidStatus.ACCEPTED) {
            throw new CustomException(BidErrorCode.BID_ALREADY_ACCEPTED);
        }

        Negotiation negotiation = Negotiation.builder()
                .bid(bid)
                .senderRole(role) // "USER" or "AGENT"
                .suggestedFee(reqDto.suggestedFee())
                .message(reqDto.message())
                .build();

        negotiationRepository.save(negotiation);
    }

    /**
     * 중개사 선택 및 매칭 확정
     */
    @Transactional
    public void acceptBid(Long propertyId, Long bidId, Long userId) {
        Property property = getPropertyAndValidateOwnership(propertyId, userId);

        // 이 매물이 이미 매칭 완료된 상태라면?
        if (property.getStatus() == PropertyStatus.MATCHED) {
            throw new CustomException(BidErrorCode.PROPERTY_ALREADY_MATCHED);
        }

        // Bid 상태 확인 및 검증
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new CustomException(BidErrorCode.BID_NOT_FOUND));

        if (!bid.getProperty().getId().equals(propertyId)) {
            throw new CustomException(BidErrorCode.BID_PROPERTY_MISMATCH);
        }

        if (bid.getStatus() == BidStatus.ACCEPTED) {
            throw new CustomException(BidErrorCode.BID_ALREADY_ACCEPTED);
        }

        // 최종 확정 수수료 결정 (가장 마지막 협상 금액, 없으면 최초 입찰 금액)
        List<Negotiation> negotiations = negotiationRepository.findAllByBidIdOrderByCreatedAtAsc(bidId);
        Double finalFee = negotiations.isEmpty() ?
                bid.getProposedFee() : negotiations.get(negotiations.size() - 1).getSuggestedFee();

        // Bid 상태를 ACCEPTED로 변경 및 최종 수수료 저장
        bid.acceptBid(finalFee);

        // 매물 상태도 '매칭 완료'로 변경
        property.matchDeal();

        // TODO: 채팅방 생성 이벤트 로직

    }

    // ================= [ 공통 검증 로직 ] =================

    /**
     * 매물 존재 여부 확인 및 집주인(USER) 권한 검증
     */
    private Property getPropertyAndValidateOwnership(Long propertyId, Long userId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new CustomException(PropertyErrorCode.PROPERTY_NOT_FOUND));

        if (!property.getUser().getId().equals(userId)) {
            throw new CustomException(PropertyErrorCode.UNAUTHORIZED_ACCESS);
        }
        return property;
    }

    /**
     * 핑퐁(역제안) API 접근 권한 검증
     * - USER(집주인)인 경우: 내 매물이 맞는지 확인
     * - AGENT(중개사)인 경우: 본인이 작성한 입찰서가 맞는지 확인
     */
    private Bid validateAccessRight(Long propertyId, Long bidId, Long userId, String role) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new CustomException(BidErrorCode.BID_NOT_FOUND));

        if (!bid.getProperty().getId().equals(propertyId)) {
            throw new CustomException(BidErrorCode.BID_PROPERTY_MISMATCH);
        }

        /**
         * 역할별 검증
         */
        if ("USER".equals(role)) { // 집주인: 해당 매물의 소유자가 맞는지 검증
            getPropertyAndValidateOwnership(propertyId, userId);
        } else if ("AGENT".equals(role)) { // 중개사: 해당 제안서(bid)를 작성한 중개사가 맞는지 검증
            if (!bid.getAgent().getUser().getId().equals(userId)) {
                throw new CustomException(BidErrorCode.UNAUTHORIZED_BID_ACCESS);
            }
        }

        return bid;
    }
}
