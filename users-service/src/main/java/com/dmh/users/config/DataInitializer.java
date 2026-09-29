package com.dmh.users.config;

import com.dmh.users.model.Rol;
import com.dmh.users.model.User;
import com.dmh.users.repository.RolRepository;
import com.dmh.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        Rol userRole = rolRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    Rol role = new Rol();
                    role.setName("ROLE_USER");
                    return rolRepository.save(role);
                });

        if (!userRepository.existsByEmail("valentina@test.com")) {

            User user = new User();

            user.setFirstName("Valentina");
            user.setLastName("Test");
            user.setDni("12345678");
            user.setEmail("valentina@test.com");
            user.setPhone("1123456789");

            user.setPassword(
                    passwordEncoder.encode("Valen1234")
            );

            user.setRole(userRole);

            userRepository.save(user);
        }
    }
}