package com.dmh.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String nombre;
    private String apellido;
    private String dni;
    private String email;
    private String telefono;
    private String cvu;   // Proveniente del accounts-service vía Feign
    private String alias; // Proveniente del accounts-service vía Feign
}