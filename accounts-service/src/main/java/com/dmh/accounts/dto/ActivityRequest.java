package com.dmh.accounts.dto;

import java.math.BigDecimal;

public record ActivityRequest(
        BigDecimal amount,
        String destination,
        String origin,
        String type
) {
}
