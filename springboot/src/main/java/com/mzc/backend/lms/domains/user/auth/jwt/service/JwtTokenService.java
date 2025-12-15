package com.mzc.backend.lms.domains.user.auth.jwt.service;

import com.mzc.backend.lms.domains.user.professor.entity.Professor;
import com.mzc.backend.lms.domains.user.student.entity.Student;
import com.mzc.backend.lms.domains.user.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 토큰 서비스
 * Access Token과 Refresh Token 생성 및 검증
 */
@Service
@RequiredArgsConstructor
public class JwtTokenService {

    @Value("${security.jwt.secret:defaultJwtSecretKeyForDevelopment123456789012345678901234567890}")
    private String secretKey;

    @Value("${security.jwt.access-token-expire-time:1800000}") // 30분 (밀리초)
    private Long accessTokenExpiration;

    @Value("${security.jwt.refresh-token-expire-time:604800000}") // 7일 (밀리초)
    private Long refreshTokenExpiration;

    /**
     * Access Token 생성
     */
    public String generateAccessToken(User user, String userType, Long userNumber) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("userType", userType); // STUDENT or PROFESSOR
        claims.put("userNumber", userNumber.toString()); // 학번 또는 교번

        return createToken(claims, user.getEmail(), accessTokenExpiration);
    }

    /**
     * 학생용 Access Token 생성
     */
    public String generateStudentAccessToken(User user, Student student) {
        return generateAccessToken(user, "STUDENT", student.getStudentNumber());
    }

    /**
     * 교수용 Access Token 생성
     */
    public String generateProfessorAccessToken(User user, Professor professor) {
        return generateAccessToken(user, "PROFESSOR", professor.getProfessorNumber());
    }

    /**
     * Refresh Token 생성
     */
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("tokenType", "REFRESH");

        return createToken(claims, user.getEmail(), refreshTokenExpiration);
    }

    /**
     * 토큰 생성
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰에서 Claims 추출
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 토큰에서 사용자 ID 추출
     */
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 토큰에서 이메일 추출
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * 토큰에서 사용자 타입 추출 (STUDENT/PROFESSOR)
     */
    public String extractUserType(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userType", String.class);
    }

    /**
     * 토큰에서 학번/교번 추출
     */
    public String extractUserNumber(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userNumber", String.class);
    }

    /**
     * 토큰 만료 시간 추출
     */
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    /**
     * 토큰 만료 여부 확인
     */
    public Boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 토큰 유효성 검증
     */
    public Boolean validateToken(String token, User user) {
        try {
            final String email = extractEmail(token);
            return (email.equals(user.getEmail()) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 토큰 유효성 검증 (이메일 없이)
     */
    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 학생 여부 확인
     */
    public boolean isStudent(String token) {
        String userType = extractUserType(token);
        return "STUDENT".equals(userType);
    }

    /**
     * 교수 여부 확인
     */
    public boolean isProfessor(String token) {
        String userType = extractUserType(token);
        return "PROFESSOR".equals(userType);
    }

    /**
     * 서명 키 생성
     */
    private SecretKey getSignKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
