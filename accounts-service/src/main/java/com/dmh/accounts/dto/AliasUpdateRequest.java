package com.dmh.accounts.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record AliasUpdateRequest(
        @JsonAlias({"alias"}) String alias
) {
}