package com.dmh.accounts.exception;

public class CardAlreadyAssociatedException extends RuntimeException {

  public CardAlreadyAssociatedException(String cardNumber) {
    super(
            "La tarjeta ya está asociada a otra cuenta: "
                    + cardNumber
    );
  }
}