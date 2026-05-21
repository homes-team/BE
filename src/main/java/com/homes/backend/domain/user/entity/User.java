package com.homes.backend.domain.user.entity;

import com.homes.backend.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity { //BaseEntity에서 생성일자 처리

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id") //유저 ID
    private Long id;

    @Column(nullable = false, length = 50) //가입 경로, 현재는 자체 회원가입만
    private String provider;

    @Column(name = "provider_id", length = 255) //소셜 제공자 ID
    private String providerId;

    @Column(nullable = false, unique = true, length = 100) //이메일
    private String email;

    @Column(length = 128) //비밀번호 (소셜 가입자: NULL 허용)
    private String password;

    @Column(nullable = false, length = 20) // 휴대폰 번호
    private String phone;

    @Column(nullable = false, length = 30) // 실명
    private String name;

    @Column(nullable = false, length = 50) // 닉네임
    private String nickname;

    @Builder.Default
    @Column(name = "is_identity_verified", nullable = false) // 실명 인증 여부 (디폴트 false)
    private boolean isIdentityVerified = false;

    @Column(nullable = false, length = 20) // 역할 (권한)
    private String role;

    @Column(name = "usage_purpose", length = 50) // 이용 목적 (예: 투자/전세 등, NULL 가능)
    private String usagePurpose;

    @Column(name = "refresh_token", length = 255) // JWT 리프레시 토큰 (NULL 가능)
    private String refreshToken;

    @Builder.Default
    @Column(name = "reputation_score", nullable = false) // 평판 점수 (기본값 36.5)
    private Float reputationScore = 36.5f;


}