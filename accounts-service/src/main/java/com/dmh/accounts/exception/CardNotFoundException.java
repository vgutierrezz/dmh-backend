package com.dmh.accounts.exception;

public class CardNotFoundException extends RuntimeException {

    public CardNotFoundException(Long cardId) {
        super("No se encontró la tarjeta con id: " + cardId);
    }
}
