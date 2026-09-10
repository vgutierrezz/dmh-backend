package com.dmh.accounts.dto;

import com.dmh.accounts.model.enums.TransactionType;

import java.time.LocalDateTime;

public record ActivityResponse(
        Long id,
        Double amount,
        String name,
        LocalDateTime dated,
        TransactionType type,
        String origin,
        String destination
) {
}
