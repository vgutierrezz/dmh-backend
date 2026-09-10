package com.dmh.accounts.util;

import com.dmh.accounts.model.enums.TypeCard;

public final class CardTypeDetector {

    private CardTypeDetector() {
    }

    public static TypeCard getCardType(String cardNumber) {

        if (cardNumber == null || cardNumber.isBlank()) {
            return TypeCard.CREDIT_CARD;
        }

        String clean = cardNumber.replace("-", "");

        if (isVisa(clean)) {
            return TypeCard.VISA;
        }

        if (isMastercard(clean)) {
            return TypeCard.MASTERCARD;
        }

        return TypeCard.CREDIT_CARD;
    }

    private static boolean isVisa(String cardNumber) {
        return cardNumber.length() == 16
                && cardNumber.startsWith("4");
    }

    private static boolean isMastercard(String cardNumber) {
        if (cardNumber.length() != 16) {
            return false;
        }

        int firstTwoDigits =
                Integer.parseInt(cardNumber.substring(0, 2));

        return firstTwoDigits >= 51
                && firstTwoDigits <= 55;
    }
}