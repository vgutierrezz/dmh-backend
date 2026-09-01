package com.dmh.auth.controller;

import com.dmh.auth.dto.AuthRequest;
import com.dmh.auth.dto.AuthResponse;
import com.dmh.auth.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response); // Retorna 200 OK con el token estructurado
        } catch (RuntimeException e) {
            // Manejo de errores local rápido para cumplir los códigos exactos del enunciado (Luego usaremos Handler Global)
            Map<String, String> errorBody = new HashMap<>();
            errorBody.clear();
            errorBody.put("error", e.getMessage());

            if (e.getMessage().equals("Usuario inexistente")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody); // 404
            } else if (e.getMessage().equals("Contraseña incorrecta")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody); // 400
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody); // 500
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            authService.logout(authorizationHeader);
            return ResponseEntity.ok(Map.of("message", "Logout exitoso"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}