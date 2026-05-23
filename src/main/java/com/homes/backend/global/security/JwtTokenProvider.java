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

    // 1. 토큰에서 유저 이메일(Subject) 끄집어내는 돋보기
    public String getEmailFromToken(String token) {
        return io.jsonwebtoken.Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 2. 토큰이 기한이 만료되지 않았는지, 유효한지 검사하는 돋보기
    public boolean validateToken(String token) {
        try {
            io.jsonwebtoken.Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // 토큰이 변조되었거나 만료되면 알아서 false를 뱉습니다.
            return false;
        }
    }
}