package com.dmh.users.repository;

import com.dmh.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Busca las credenciales o datos de un usuario por su email
    Optional<User> findByEmail(String email);

    // Métodos de validación rápida para el registro de usuarios
    boolean existsByEmail(String email);
    boolean existsByDni(String dni);
}