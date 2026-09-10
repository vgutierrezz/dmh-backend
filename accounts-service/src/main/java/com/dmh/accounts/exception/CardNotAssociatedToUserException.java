package com.dmh.accounts.exception;

public class CardNotAssociatedToUserException extends RuntimeException {

    public CardNotAssociatedToUserException(Long userId, Long cardId) {
        super("La tarjeta " + cardId +
                " no está asociada al usuario " + userId);
    }
}
