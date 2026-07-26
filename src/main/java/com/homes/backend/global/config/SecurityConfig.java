package com.homes.backend.global.config;

import com.homes.backend.global.exception.GlobalErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.homes.backend.global.security.CustomUserDetailsService;
import com.homes.backend.global.security.JwtAuthenticationFilter;
import com.homes.backend.global.security.JwtTokenProvider;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    //스웨거가 토큰을 인식하도록 설정 추가함
    @Bean
    public OpenAPI openAPI() {
        String securityJwtName = "JWT_Token";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(securityJwtName);
        Components components = new Components().addSecuritySchemes(securityJwtName,
                new SecurityScheme()
                        .name("Authorization")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        return new OpenAPI()
                .addSecurityItem(securityRequirement)
                .components(components);
    }

    //비밀번호를 암호화해 줄 기계를 스프링 시스템에 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception
                        // 로그인 안 한 유저 (401 에러)
                        .authenticationEntryPoint((request, response, authException) -> {
                            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, GlobalErrorCode.UNAUTHORIZED);
                        })
                        // 권한 없는 유저 (403 에러)
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, GlobalErrorCode.FORBIDDEN);
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/users/login",
                                "/users/signup",
                                "/users/oauth/**",
                                "/users/check-email",
                                "/users/emails/**",
                                "/users/refresh",
                                "/users/realtors"
                        ).permitAll()

                        .requestMatchers(
                                org.springframework.http.HttpMethod.GET,
                                "/properties",
                                "/properties/{propertyId}",
                                "/properties/map"
                        ).permitAll()

                        // Swagger 관련 프리패스 주소 (이건 기존 yml 설정에 맞게 유지)
                        .requestMatchers("/api-docs", "/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/swagger-resources", "/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService, redisTemplate),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 공통 에러 응답을 JSON으로 만들어주는 헬퍼 메서드
      */
    private void sendErrorResponse(HttpServletResponse response, int status, GlobalErrorCode errorCode) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("isSuccess", false);
        errorDetails.put("code", errorCode.getCode());
        errorDetails.put("message", errorCode.getMessage());
        
        try { // JSON으로 변환하여 화면에 출력
            response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}