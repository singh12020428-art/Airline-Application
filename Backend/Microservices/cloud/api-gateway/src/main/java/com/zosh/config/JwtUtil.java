package com.zosh.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtUtil {

    private final SecretKey key =
            Keys.hmacShaKeyFor(JwtConstant.SECRET_KEY.getBytes());

    private Claims getClaims(String token) {

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {

        try {
            getClaims(token);
            return true;
        }
        catch (Exception ex) {
            return false;
        }
    }

    public String extractEmail(String token) {

        return getClaims(token)
                .get("email", String.class);
    }

    public String extractAuthorities(String token) {

        return getClaims(token)
                .get("authorities", String.class);
    }

    public Long extractUserId(String token) {

        return getClaims(token)
                .get("userId", Long.class);
    }

}