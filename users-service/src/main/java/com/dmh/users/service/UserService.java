package com.dmh.users.service;

import com.dmh.users.client.AccountClient;
import com.dmh.users.dto.AccountResponse;
import com.dmh.users.dto.UserRegisterRequest;
import com.dmh.users.dto.UserResponse;
import com.dmh.users.model.Rol;
import com.dmh.users.model.User;
import com.dmh.users.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AccountClient accountClient;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, AccountClient accountClient) {
        this.userRepository = userRepository;
        this.accountClient = accountClient;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Transactional
    public UserResponse registerUser(UserRegisterRequest request) {
        // 1. Validate uniqueness of Email and DNI
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya se encuentra registrado");
        }
        if (userRepository.existsByDni(request.getDni())) {
            throw new IllegalArgumentException("El DNI ya se encuentra registrado");
        }

        // 2. Map DTO to User Entity and encode password with BCrypt
        User user = new User();
        user.setNombre(request.getNombre());
        user.setApellido(request.getApellido());
        user.setDni(request.getDni());
        user.setEmail(request.getEmail());
        user.setTelefono(request.getTelefono());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Assign default "USER" role (ID: 1)
        Rol defaultRole = new Rol(1L, "USER");
        user.setRole(defaultRole);

        // 3. Save user in users_db
        User savedUser = userRepository.save(user);

        // 4. Synchronous call via Feign to generate the digital account in accounts_db
        AccountResponse account = accountClient.createAccount(savedUser.getId());

        // 5. Build and return unified response payload
        return new UserResponse(
                savedUser.getId(),
                savedUser.getNombre(),
                savedUser.getApellido(),
                savedUser.getDni(),
                savedUser.getEmail(),
                savedUser.getTelefono(),
                account.getCvu(),
                account.getAlias()
        );
    }
}