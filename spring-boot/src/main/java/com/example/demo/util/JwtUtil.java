package com.example.demo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

/**
 * JWT 工具類
 * 負責產生與驗證 JWT Token
 */
@Component
public class JwtUtil {
    // 建議將金鑰與過期時間放到 application.properties
    private static final String SECRET_KEY = "my-very-secret-key-which-should-be-long-enough-123456";
    private static final long EXPIRATION_MS = 86400000; // 1 天

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // 產生 JWT Token
    public String generateToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 解析 JWT Token
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 取得 subject（通常是 email 或 userId）
    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 驗證 token 是否有效
    public boolean isTokenValid(String token, String subject) {
        final String tokenSubject = extractSubject(token);
        return (tokenSubject.equals(subject) && !isTokenExpired(token));
    }

    // 判斷 token 是否過期
    private boolean isTokenExpired(String token) {
        final Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
    }
}

