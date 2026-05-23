package com.homes.backend.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 요청 헤더에서 "Authorization: Bearer [토큰]" 문자열을 추출합니다.
        String token = resolveToken(request);

        // 2. 토큰이 존재하고, 우리 비밀키로 검증했을 때 진짜가 맞다면?
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            // 3. 토큰에서 유저 이메일을 꺼냅니다.
            String email = jwtTokenProvider.getEmailFromToken(token);

            // 4. DB에서 유저 정보를 가득 담은 UserPrincipal을 로드합니다.
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // 5. 시큐리티 전용 "인증 도장(Authentication)"을 쾅 찍어줍니다.
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 6. 🌟 핵심: 시큐리티 비밀 금고(Context)에 이 인증 도장을 집어넣습니다!
            // 이제 컨트롤러에서 이 유저가 누구인지 언제든 꺼내 쓸 수 있게 됩니다.
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 문지기 역할 끝! 다음 필터나 컨트롤러로 요청을 넘겨줍니다.
        filterChain.doFilter(request, response);
    }

    // 헤더에서 토큰 알맹이만 쏙 빼오는 메서드
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 뒤의 토큰 문자열만 리턴
        }
        return null;
    }
}