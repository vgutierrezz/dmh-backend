package com.dmh.auth.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationTime;

    private Key key;

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
                .subject(email)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationTime))
                .signWith((SecretKey) key) // Casteo correcto para la firma en JJWT 0.12.x
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            // Se usa .parser() en lugar de .parserBuilder()
            Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) key) // Firma segura para JJWT moderno
                    .build()
                    .parseSignedClaims(token); // En lugar de parseClaimsJws
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload() // En lugar de getBody()
                .getSubject();
    }
}