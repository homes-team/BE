package com.homes.backend.domain.realtor.entity;

import com.homes.backend.domain.user.entity.User;
import com.homes.backend.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "agent")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Agent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agent_id") // 프로필 ID
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder.Default
    @Column(name = "is_verified", nullable = false) // 중개사 자격 인증 여부(디폴트 false)
    private boolean isVerified = false;

    @Column(name = "business_num", nullable = false, unique = true, length = 50) // 사업자 등록번호
    private String businessNum;

    @Column(name = "profile_image_url", length = 255) // 중개사 프로필 사진 경로
    private String profileImageUrl;

    @Column(name = "business_cert_url", length = 255) // 사업자등록증 서류 이미지 경로
    private String businessCertUrl;

    @Column(name = "agent_cert_url", length = 255) // 중개사무소 등록증 서류 이미지 경로
    private String agentCertUrl;

    @Column(name = "office_name", length = 50) // 중개사무소 이름
    private String officeName;
}
