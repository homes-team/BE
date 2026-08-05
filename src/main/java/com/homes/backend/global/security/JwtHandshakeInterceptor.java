package com.homes.backend.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * 웹소켓은 REST처럼 매 요청마다 인증 필터가 도는 게 아니라, 연결을 맺는 이 시점(핸드셰이크)에 딱 한 번만 인증 기회가 있다.
 * 여기서 검증한 유저 정보를 attributes에 담아두면, 스프링이 세션 전체에 걸쳐 계속 들고 다녀준다.
 */
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = extractToken(request);

        if (!StringUtils.hasText(token) || !jwtTokenProvider.validateToken(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        String isBlackList = (String) redisTemplate.opsForValue().get("BLACK:" + token);
        if (isBlackList != null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        String email = jwtTokenProvider.getEmailFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        UserPrincipal userPrincipal = (UserPrincipal) userDetails;

        attributes.put("userId", userPrincipal.getId());
        attributes.put("role", userPrincipal.getRole());

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {
        // 별도 후처리 없음
    }

    // 브라우저 기본 WebSocket API는 커스텀 헤더를 못 붙이므로, 접속 URL의 쿼리 파라미터로 토큰을 받는다 (예: /ws/chats?token=xxx)
    private String extractToken(ServerHttpRequest request) {
        String query = request.getURI().getQuery();
        if (query == null) {
            return null;
        }
        for (String param : query.split("&")) {
            if (param.startsWith("token=")) {
                return param.substring("token=".length());
            }
        }
        return null;
    }
}
