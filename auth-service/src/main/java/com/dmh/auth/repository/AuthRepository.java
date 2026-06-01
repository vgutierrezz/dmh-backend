package com.dmh.auth.repository;

import com.dmh.auth.model.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<UserAuth, Long> {
    // Busca las credenciales del usuario por email para el login
    Optional<UserAuth> findByEmail(String email);
}