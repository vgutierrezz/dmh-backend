package com.dmh.accounts.service;


import com.dmh.accounts.dto.CardRequest;
import com.dmh.accounts.dto.CardResponse;
import com.dmh.accounts.model.Account;
import com.dmh.accounts.model.Card;
import com.dmh.accounts.model.enums.TypeCard;
import com.dmh.accounts.repository.CardRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final AccountService accountService;

    public List<CardResponse> createCard(Long userId, CardRequest cardRequest) {
        Optional<Account> accountOptional = accountService.findByUserId(userId);
        Card card = new Card(
                null,
                Long.parseLong(cardRequest.number()),
                cardRequest.name(),
                cardRequest.expiration(),
                cardRequest.cvc(),
                accountOptional.map(Account::getId).orElse(null),
                CardTypeDetector.getCardType(cardRequest.number())
        );
        cardRepository.save(card);
        return List.of(); // Retorna una lista vacía si no se encuentra la cuenta
    }

    public List<CardResponse> getCardsByUserId(Long userId) {
        Optional<Account> accountOptional = accountService.findByUserId(userId);
        if (!accountOptional.isPresent()) {
            return List.of(); // Retorna una lista vacía si no se encuentra la cuenta
        }
        return cardRepository.findAll().stream()
                .filter(card -> card.getAccountId().equals(accountOptional.get().getId()))
                .map(card -> new CardResponse(
                        card.getId().toString(),
                        card.getNumber().toString(),
                        card.getName(),
                        card.getType().name()
                ))
                .toList();
    }

    public void deteleCard(Long cardId) {
        cardRepository.deleteById(cardId);
    }

    public class CardTypeDetector {

        public static TypeCard getCardType(String cardNumber) {
            if (cardNumber == null || cardNumber.isEmpty()) {
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

        public static boolean isVisa(String cardNumber) {
            String clean = cardNumber.replace("-", "");
            return clean.length() == 16 && clean.startsWith("4");
        }

        public static boolean isMastercard(String cardNumber) {
            String clean = cardNumber.replace("-", "");
            if (clean.length() != 16) {
                return false;
            }

            int firstTwoDigits = Integer.parseInt(clean.substring(0, 2));
            return firstTwoDigits >= 51 && firstTwoDigits <= 55;
        }
    }
}
