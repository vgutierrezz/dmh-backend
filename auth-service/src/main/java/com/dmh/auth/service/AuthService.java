package com.dmh.auth.service;

import com.dmh.auth.config.JwtProvider;
import com.dmh.auth.dto.AuthRequest;
import com.dmh.auth.dto.AuthResponse;
import com.dmh.auth.exception.InvalidPasswordException;
import com.dmh.auth.exception.UserNotFoundException;
import com.dmh.auth.model.UserAuth;
import com.dmh.auth.repository.AuthRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthService(AuthRepository authRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.authRepository = authRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    public AuthResponse login(AuthRequest request) {
        UserAuth user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Usuario inexistente"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Contraseña incorrecta");
        }

        String token = jwtProvider.generateToken(user.getEmail(), user.getRole().getNombre());
        return new AuthResponse(token);
    }

    public void logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token requerido");
        }

        String token = authorizationHeader.substring(7);

        if (!jwtProvider.validateToken(token)) {
            throw new IllegalArgumentException("Token inválido");
        }
    }
}