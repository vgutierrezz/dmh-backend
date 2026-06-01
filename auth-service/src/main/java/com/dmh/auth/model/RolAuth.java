package com.dmh.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles") // Nombre de la tabla física en users_db
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre; // Guardará valores como "USER" o "ADMIN"
}