package com.homes.backend.domain.property.repository;

import com.homes.backend.domain.property.entity.Property;
import com.homes.backend.domain.property.entity.PropertyReport;
import com.homes.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyReportRepository extends JpaRepository<PropertyReport,Long> {
    /**
     * 이미 신고한 매물인지 확인
     */
    boolean existsByPropertyAndReporter(Property property, User reporter);

    /**
     * 내가 신고한 내역 리스트 조회
     */
    List<PropertyReport> findAllByReporterId(Long reporterId);
}
