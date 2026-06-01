package com.dmh.auth.model;

import jakarta.persistence.*; // Uso estricto de Jakarta para Spring Boot 3.x
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users") // Apunta a la misma tabla física que creará el users-service
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    // Relación simple con la tabla de roles requerida por la cátedra
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private RolAuth role;
}