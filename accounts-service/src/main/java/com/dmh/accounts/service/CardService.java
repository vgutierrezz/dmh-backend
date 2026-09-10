package com.dmh.accounts.service;


import com.dmh.accounts.dto.CardRequest;
import com.dmh.accounts.dto.CardResponse;
import com.dmh.accounts.exception.AccountNotFoundException;
import com.dmh.accounts.exception.CardNotAssociatedToUserException;
import com.dmh.accounts.model.Account;
import com.dmh.accounts.model.Card;
import com.dmh.accounts.repository.CardRepository;
import com.dmh.accounts.util.CardTypeDetector;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final AccountService accountService;

    public CardResponse createCard(Long userId, CardRequest cardRequest) {

        Account account = accountService.findByUserId(userId)
                .orElseThrow(() ->
                        new AccountNotFoundException(userId.toString()));

        Card card = new Card(
                null,
                Long.parseLong(cardRequest.number()),
                cardRequest.name(),
                cardRequest.expiration(),
                cardRequest.cvc(),
                account,
                CardTypeDetector.getCardType(cardRequest.number())
        );

        Card savedCard = cardRepository.save(card);

        return new CardResponse(
                savedCard.getId().toString(),
                savedCard.getNumber().toString(),
                savedCard.getName(),
                savedCard.getType().name()
        );
    }

    public List<CardResponse> getCardsByUserId(Long userId) {

        Account account = accountService.findByUserId(userId)
                .orElseThrow(() -> new AccountNotFoundException(userId.toString()));

        return cardRepository.findByAccountId(account.getId())
                .stream()
                .map(card -> new CardResponse(
                        card.getId().toString(),
                        card.getNumber().toString(),
                        card.getName(),
                        card.getType().name()
                ))
                .toList();
    }

    public void deleteCard(Long userId, Long cardId) {

        Card card = cardRepository
                .findByIdAndAccountUserId(cardId, userId)
                .orElseThrow(() ->
                        new CardNotAssociatedToUserException(
                                userId,
                                cardId
                        ));

        cardRepository.delete(card);
    }

}
