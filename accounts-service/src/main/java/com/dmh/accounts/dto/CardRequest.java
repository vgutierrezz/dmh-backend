package com.dmh.accounts.dto;

public record CardRequest(
        String number,
        String expiration,
        String name,
        String cvc
) {
}
