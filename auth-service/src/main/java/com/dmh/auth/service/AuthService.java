package com.dmh.auth.service;

import com.dmh.auth.config.JwtProvider;
import com.dmh.auth.dto.AuthRequest;
import com.dmh.auth.dto.AuthResponse;
import com.dmh.auth.model.UserAuth;
import com.dmh.auth.repository.AuthRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // Inyección por constructor (Buena práctica recomendada)
    public AuthService(AuthRepository authRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.authRepository = authRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    public AuthResponse login(AuthRequest request) {
        // 1. Validar que el usuario exista (Sino, lanzamos excepción que atrapará el ControllerAdvice)
        UserAuth user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario inexistente")); // Bad Request 404 sugerido por la cátedra

        // 2. Validar que la contraseña sea correcta contra el hash BCrypt
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Contraseña incorrecta"); // Error 400 sugerido por la cátedra
        }

        // 3. Generar el token JWT incluyendo el rol del usuario
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