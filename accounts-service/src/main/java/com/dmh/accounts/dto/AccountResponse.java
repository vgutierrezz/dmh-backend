package com.dmh.accounts.dto;

import java.math.BigDecimal;

public record AccountResponse (
        String id,
        String userId,
        BigDecimal balance,
        String cvu,
        String alias)
{
}
