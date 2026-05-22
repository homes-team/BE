package com.homes.backend.global.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final Key signingKey;
    private final long validityInMilliseconds = 3600000; // 1시간

    // 생성자를 통해 yml에 있는 jwt.secret 값 가져오기
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // 토큰 굽는 메서드
    public String createToken(Long userId, String email) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setSubject(email) // 토큰 주인의 이메일
                .claim("userId", userId) // 토큰 내부에 고유 ID 숨겨놓기 (나중에 @AuthUser로 꺼낼 알맹이)
                .setIssuedAt(now)
                .setExpiration(validity) // 만료 시간 설정
                .signWith(signingKey, SignatureAlgorithm.HS256) //암호화 서명, 위에서 만든 세팅키 사용
                .compact();
    }
}