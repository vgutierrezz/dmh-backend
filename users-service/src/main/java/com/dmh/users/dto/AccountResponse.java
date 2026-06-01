package com.dmh.users.dto;

import lombok.Data;

@Data
public class AccountResponse {
    private Long id;
    private Long userId;
    private String cvu;
    private String alias;
    private Double balance;
}