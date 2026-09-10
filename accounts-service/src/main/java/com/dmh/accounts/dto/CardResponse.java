package com.dmh.accounts.dto;

public record CardResponse (
    String id,
    String number,
    String name,
    String type
) {

}
