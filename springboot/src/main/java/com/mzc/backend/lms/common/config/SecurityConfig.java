package com.mzc.backend.lms.common.config;

import com.mzc.backend.lms.domains.user.auth.jwt.filter.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Map;

import java.util.Arrays;

/**
 * Spring Security 설정
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (JWT 사용)
            .csrf(AbstractHttpConfigurer::disable)

            // CORS 설정
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 세션 관리 정책 설정 (Stateless)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 요청 권한 설정
            .authorizeHttpRequests(authorize -> authorize
                // 인증 없이 접근 가능한 경로
                .requestMatchers(
                    "/api/auth/**",
                    "/api/public/**",
                    "/health",
                    "/error",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/swagger-resources/**",
                    "/v3/api-docs/**"
                ).permitAll()

                // 학생 게시판 - 학생만 접근 가능 (조회, 작성, 수정, 삭제)
                .requestMatchers("/api/v1/boards/STUDENT/**").hasAuthority("STUDENT")

                // 교수 게시판 - 교수만 접근 가능 (조회, 작성, 수정, 삭제)
                .requestMatchers("/api/v1/boards/PROFESSOR/**").hasAuthority("PROFESSOR")

                // 나머지 게시판 API - 인증된 사용자 접근 가능
                .requestMatchers("/api/v1/boards/**").authenticated()
                .requestMatchers("/api/v1/board/**").authenticated()  // 댓글 API

                // 학생 전용 API
                .requestMatchers("/api/student/**").hasAuthority("STUDENT")

                // 교수 전용 API
                .requestMatchers("/api/professor/**").hasAuthority("PROFESSOR")

                // 나머지 요청은 인증 필요
                .anyRequest().authenticated()
            )

            // JWT 필터 추가
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

            // 인증 실패 시 401 Unauthorized 응답
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");

                    Map<String, Object> errorResponse = Map.of(
                        "status", 401,
                        "error", "Unauthorized",
                        "message", "인증이 필요합니다. 토큰이 없거나 만료되었습니다."
                    );

                    new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
                })
            );

        return http.build();
    }

    /**
     * CORS 설정
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE","PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 패스워드 인코더
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
