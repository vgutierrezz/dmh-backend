package com.dmh.users.service;

import com.dmh.users.client.AccountClient;
import com.dmh.users.client.AuthClient;
import com.dmh.users.dto.AccountResponse;
import com.dmh.users.dto.UserRegisterRequest;
import com.dmh.users.dto.UserResponse;
import com.dmh.users.model.Rol;
import com.dmh.users.model.User;
import com.dmh.users.repository.RolRepository;
import com.dmh.users.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RolRepository rolRepository;
    private final AccountClient accountClient;
    private final AuthClient authClient;
    private final PasswordEncoder passwordEncoder;

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    public UserResponse registerUser(UserRegisterRequest request) {
        UserResponse response = createUserAndAccount(request);

        Map<String, String> credentials = Map.of(
                "email", response.getEmail(),
                "password", request.getPassword()
        );

        Map<String, String> authResponse = authClient.login(credentials);
        String token = authResponse.get("token");
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("No se pudo generar el token para el usuario registrado");
        }

        response.setToken(token);
        response.setAccessToken(token);
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    protected UserResponse createUserAndAccount(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya se encuentra registrado");
        }
        if (userRepository.existsByDni(request.getDni())) {
            throw new IllegalArgumentException("El DNI ya se encuentra registrado");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDni(request.getDni());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Rol defaultRole = rolRepository.findByName("USER").orElseGet(() -> {
            Rol r = new Rol();
            r.setName("USER");
            return rolRepository.save(r);
        });
        user.setRole(defaultRole);

        User savedUser = userRepository.save(user);

        AccountResponse account = accountClient.createAccount(savedUser.getId());
        log.info("Cuenta creada para usuario {}: {}", savedUser.getId(), account);

        log.info("Usuario registrado correctamente: {}", savedUser.getId());

        return new UserResponse(
                savedUser.getId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getDni(),
                savedUser.getEmail(),
                savedUser.getPhone()
        );
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getDni(),
                user.getEmail(),
                user.getPhone()
        );
    }

    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getDni(),
                user.getEmail(),
                user.getPhone()
        );
    }
}