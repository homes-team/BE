package com.homes.backend.domain.verification.service;

import com.homes.backend.domain.property.entity.Property;
import com.homes.backend.domain.property.exception.PropertyErrorCode;
import com.homes.backend.domain.property.repository.PropertyRepository;
import com.homes.backend.domain.realtor.entity.Agent;
import com.homes.backend.domain.realtor.exception.RealtorErrorCode;
import com.homes.backend.domain.realtor.repository.AgentRepository;
import com.homes.backend.domain.verification.dto.request.RealtorVerificationReqDto;
import com.homes.backend.domain.verification.dto.response.VerificationStatusRespDto;
import com.homes.backend.domain.verification.entity.RealtorVerification;
import com.homes.backend.domain.verification.entity.VerificationStatus;
import com.homes.backend.domain.verification.exception.VerificationErrorCode;
import com.homes.backend.domain.verification.repository.RealtorVerificationRepository;
import com.homes.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RealtorVerificationService {

    private final PropertyRepository propertyRepository;
    private final AgentRepository agentRepository;
    private final RealtorVerificationRepository realtorVerificationRepository;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    private static final double MAX_ALLOWED_DISTANCE_METERS = 100.0; // 허용 오차 반경 100m

    /**
     * 중개사 현장 인증 요청
     */
    @Transactional
    public VerificationStatus verifyOnSite(Long propertyId, Long userId, RealtorVerificationReqDto reqDto) {
        Property property = propertyRepository.findByIdWithPessimisticLock(propertyId)
                .orElseThrow(() -> new CustomException(PropertyErrorCode.PROPERTY_NOT_FOUND));

        Agent agent = agentRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(RealtorErrorCode.AGENT_NOT_FOUND));

        // 미승인 중개사의 접근 차단
        if (!agent.isVerified()) {
            throw new CustomException(RealtorErrorCode.UNAPPROVED_AGENT);
        }

        // 이미 승인된 매물인지 확인하여 중복 방지
        RealtorVerification latestVerification = realtorVerificationRepository
                .findTopByPropertyIdOrderByRequestedAtDesc(propertyId)
                .orElse(null);

        // 이미 승인(APPROVED) 상태라면 새로 만든 에러 발생
        if (latestVerification != null && latestVerification.getStatus() == VerificationStatus.APPROVED) {
            throw new CustomException(VerificationErrorCode.ALREADY_VERIFIED);
        }

        // 중개사가 현재 서 있는 GPS 위치 Point 생성 (경도, 위도 순서)
        Point realtorLocation = geometryFactory.createPoint(new Coordinate(reqDto.longitude(), reqDto.latitude()));

        // PostGIS 네이티브 쿼리를 이용해 실제 매물 위치와의 거리 차이(m) 계산
        Double distanceMeter = propertyRepository.calculateDistanceToProperty(propertyId, realtorLocation);

        // 거리가 100m 이내면 APPROVED, 넘어가면 REJECTED
        VerificationStatus status = (distanceMeter != null && distanceMeter <= MAX_ALLOWED_DISTANCE_METERS)
                ? VerificationStatus.APPROVED
                : VerificationStatus.REJECTED;

        // 인증 내역 DB 저장
        RealtorVerification verification = RealtorVerification.builder()
                .property(property)
                .agent(agent)
                .photoUrl(reqDto.photoUrl())
                .location(realtorLocation)
                .distanceMeter(distanceMeter)
                .status(status)
                .build();

        realtorVerificationRepository.save(verification);

        return status;
    }

    /**
     * 매물 인증 상태 조회
     */
    @Transactional(readOnly = true)
    public VerificationStatusRespDto getVerificationStatus(Long propertyId) {
        // 매물이 실제로 존재하는지 먼저 검증 (없으면 404 에러 발생)
        if (!propertyRepository.existsById(propertyId)) {
            throw new CustomException(PropertyErrorCode.PROPERTY_NOT_FOUND);
        }

        RealtorVerification latestVerification = realtorVerificationRepository
                .findTopByPropertyIdOrderByRequestedAtDesc(propertyId)
                .orElse(null);

        // 연산 없이 DB에 저장된 상태값만 그대로 반환하므로 과부하 0%
        boolean isRealtorVerified = (latestVerification != null && latestVerification.getStatus() == VerificationStatus.APPROVED);
        VerificationStatus realtorStatus = (latestVerification != null) ? latestVerification.getStatus() : null;

        return new VerificationStatusRespDto(
                false,              // isOwnerVerified (현재는 무조건 false)
                isRealtorVerified,  // isRealtorVerified
                realtorStatus       // realtorStatus
        );
    }
}
