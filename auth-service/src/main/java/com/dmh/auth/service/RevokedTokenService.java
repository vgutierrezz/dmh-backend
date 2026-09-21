package com.dmh.auth.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RevokedTokenService {

    private final Set<String> revokedTokens =
            ConcurrentHashMap.newKeySet();

    public void revokeToken(String token) {
        revokedTokens.add(token);
    }

    public boolean isRevoked(String token) {
        return revokedTokens.contains(token);
    }
}