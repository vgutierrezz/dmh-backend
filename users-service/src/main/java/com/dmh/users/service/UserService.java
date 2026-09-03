package com.dmh.users.service;

import com.dmh.users.client.AccountClient;
import com.dmh.users.dto.AccountResponse;
import com.dmh.users.dto.UserRegisterRequest;
import com.dmh.users.dto.UserResponse;
import com.dmh.users.model.Rol;
import com.dmh.users.model.User;
import com.dmh.users.repository.RolRepository;
import com.dmh.users.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RolRepository rolRepository;
    private final AccountClient accountClient;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RolRepository rolRepository, AccountClient accountClient) {
        this.userRepository = userRepository;
        this.rolRepository = rolRepository;
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
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDni(request.getDni());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Assign default "USER" role: fetch from DB or create if missing
        Rol defaultRole = rolRepository.findByName("USER").orElseGet(() -> {
            Rol r = new Rol();
            r.setName("USER");
            return rolRepository.save(r);
        });
        user.setRole(defaultRole);

        // 3. Save user in users_db
        User savedUser = userRepository.save(user);

        // 4. Synchronous call via Feign to generate the digital account in accounts_db
        AccountResponse account = accountClient.createAccount(savedUser.getId());

        // 5. Build and return unified response payload
        return new UserResponse(
                savedUser.getId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getDni(),
                savedUser.getEmail(),
                savedUser.getPhone(),
                account.getCvu(),
                account.getAlias()
        );
    }

    public Double getBalance(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        AccountResponse account = accountClient.getByUserId(userId);
        if (account == null) throw new RuntimeException("Account not found");
        return account.getBalance();
    }
}