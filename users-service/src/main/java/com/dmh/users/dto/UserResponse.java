package com.dmh.users.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String dni;
    private String email;
    private String phone;
    private String token;
    private String accessToken;

    public UserResponse(Long id, String firstName, String lastName, String dni, String email, String phone) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dni = dni;
        this.email = email;
        this.phone = phone;
    }

    public UserResponse(Long id, String firstName, String lastName, String dni, String email, String phone, String token) {
        this(id, firstName, lastName, dni, email, phone);
        this.token = token;
        this.accessToken = token;
    }

    public void setToken(String token) {
        this.token = token;
        this.accessToken = token;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        this.token = accessToken;
    }
}