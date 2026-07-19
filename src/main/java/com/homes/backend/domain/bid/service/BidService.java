package com.homes.backend.domain.bid.service;

import com.homes.backend.domain.bid.dto.response.BidListRespDto;
import com.homes.backend.domain.bid.repository.BidRepository;
import com.homes.backend.domain.property.entity.Property;
import com.homes.backend.domain.property.exception.PropertyErrorCode;
import com.homes.backend.domain.property.repository.PropertyRepository;
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

    /**
     * 특정 매물의 입찰 제안서 리스트 조회 (집주인 전용)
     */
    public List<BidListRespDto> getPropertyBids(Long propertyId, Long userId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new CustomException(PropertyErrorCode.PROPERTY_NOT_FOUND));

        /**
         * 조회 요청을 보낸 유저가 이 매물을 올린 집주인이 맞는지 확인
          */
        if (!property.getUser().getId().equals(userId)) {
            throw new CustomException(GlobalErrorCode.FORBIDDEN); // "권한이 없습니다" 에러 반환
        }

        return bidRepository.findAllByPropertyIdOrderByCreatedAtDesc(propertyId).stream()
                .map(BidListRespDto::from)
                .toList();
    }
}
