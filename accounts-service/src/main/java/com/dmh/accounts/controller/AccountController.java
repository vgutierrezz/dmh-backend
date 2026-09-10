package com.dmh.accounts.controller;

import com.dmh.accounts.dto.*;
import com.dmh.accounts.model.Account;
import com.dmh.accounts.service.AccountService;
import com.dmh.accounts.service.ActivityService;
import com.dmh.accounts.service.CardService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final ActivityService activityService;
    private final CardService cardService;

    @PostMapping("/internal/create")
    public AccountResponse createAccount(@RequestParam Long userId) {
        return accountService.createAccount(userId);
    }

    @GetMapping()
    public List<AccountResponse> getAll() {
        return accountService.findAll().stream()
                .map(acc -> new AccountResponse(
                        String.valueOf(acc.getId()),
                        String.valueOf(acc.getUserId()),
                        acc.getBalance(),
                        acc.getCvu(),
                        acc.getAlias()
                ))
                .toList();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<AccountResponse> getByUserId(@PathVariable Long userId) {
        return accountService.findByUserId(userId)
                .map(acc -> ResponseEntity.ok(new AccountResponse(
                        String.valueOf(acc.getId()),
                        String.valueOf(acc.getUserId()),
                        acc.getBalance(),
                        acc.getCvu(),
                        acc.getAlias()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}/activity")
    public ResponseEntity<List<ActivityResponse>> getAccountActivity(
            @PathVariable Long userId) {

        Optional<Account> accountOptional = accountService.findByUserId(userId);

        if (accountOptional.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<ActivityResponse> activities =
                activityService.findByAccountId(accountOptional.get().getId());

        return ResponseEntity.ok(activities);
    }

    @GetMapping("/user/{userId}/cards")
    public List<CardResponse> getCardsByUserId(@PathVariable Long userId) {
        List<CardResponse> cards = cardService.getCardsByUserId(userId);
        if (cards.isEmpty()) {
            return List.of(); // Retorna una lista vacía si no se encuentran tarjetas
        }
        return cards;
    }

    @PostMapping("/user/{userId}/cards")
    public ResponseEntity<CardResponse> createCard(
            @PathVariable Long userId,
            @RequestBody CardRequest cardRequest) {

        CardResponse response =
                cardService.createCard(userId, cardRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @DeleteMapping("/user/{userId}/cards/{cardId}")
    public ResponseEntity<Void> deleteCard(
            @PathVariable Long userId,
            @PathVariable Long cardId) {

        cardService.deleteCard(userId, cardId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/user/{userId}/transfers")
    public ResponseEntity<ActivityResponse> createTransfer(@PathVariable Long userId, @RequestBody ActivityRequest transferRequest) {
        accountService.createTransfer(userId, transferRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/activity/{activityId}")
    public ResponseEntity<ActivityResponse> getActivity(
            @PathVariable Long activityId) {

        return ResponseEntity.ok(
                activityService.findById(activityId)
        );
    }
}