package com.dmh.auth.config;

import com.dmh.auth.service.RevokedTokenService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.*;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationTime;

    private Key key;

    private final RevokedTokenService revokedTokenService;

    public JwtProvider(RevokedTokenService revokedTokenService) {
        this.revokedTokenService = revokedTokenService;
    }

    @PostConstruct
    protected void init() {
        // Inicializa la clave usando la firma Base64 del yml
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
    }

    public String generateToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .claims(claims)
                .id(UUID.randomUUID().toString())
                .subject(email)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationTime))
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {

        System.out.println("VALIDATING TOKEN");
        System.out.println("TOKEN: " + token);

        boolean revoked = revokedTokenService.isRevoked(token);

        System.out.println("IS REVOKED: " + revoked);

        if (revoked) {
            System.out.println("TOKEN REJECTED - REVOKED");
            return false;
        }

        try {
            Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token);
            System.out.println("TOKEN VALID");
            return true;

        } catch (Exception e) {
            System.out.println("TOKEN INVALID");
            e.printStackTrace();

            return false;
        }
    }
}