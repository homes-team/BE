package com.homes.backend.domain.property.service;

import com.homes.backend.domain.property.dto.response.PropertyListRespDto;
import com.homes.backend.domain.property.entity.Property;
import com.homes.backend.domain.property.entity.RecentView;
import com.homes.backend.domain.property.exception.PropertyErrorCode;
import com.homes.backend.domain.property.repository.PropertyRepository;
import com.homes.backend.domain.property.repository.RecentViewRepository;
import com.homes.backend.domain.user.entity.User;
import com.homes.backend.domain.user.exception.UserErrorCode;
import com.homes.backend.domain.user.repository.UserRepository;
import com.homes.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RecentViewService {
    private final RecentViewRepository recentViewRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;

    /**
     * 1. 최근 본 방 기록 추가 및 갱신 (Upsert)
     */
    public void addRecentView(Long userId, Long propertyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new CustomException(PropertyErrorCode.PROPERTY_NOT_FOUND));

        Optional<RecentView> existingView = recentViewRepository.findByUserAndProperty(user, property);

        if (existingView.isPresent()) { // 이미 본 방일 시
            existingView.get().updateViewTime(); // 조회 시간만 업데이트
        } else { // 처음 본 방일 시
            RecentView newView = RecentView.builder()
                    .user(user)
                    .property(property)
                    .build();
            recentViewRepository.save(newView);
        }
    }

    /**
     * 2. 내 최근 본 방 목록 조회
     */
    @Transactional(readOnly = true)
    public List<PropertyListRespDto> getMyRecentViews(Long userId) {
        return recentViewRepository.findTop20ByUserIdOrderByViewedAtDesc(userId).stream()
                .map(recentView -> PropertyListRespDto.from(recentView.getProperty()))
                .toList();
    }
}
