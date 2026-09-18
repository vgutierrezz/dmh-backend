package com.dmh.accounts.exception;

public class AliasAlreadyExistsException extends RuntimeException {
    public AliasAlreadyExistsException(String alias) {
        super("El alias '" + alias + "' ya está en uso");
    }
}