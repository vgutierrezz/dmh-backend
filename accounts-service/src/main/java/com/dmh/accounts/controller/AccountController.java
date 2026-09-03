package com.dmh.accounts.controller;

import com.dmh.accounts.model.Account;
import com.dmh.accounts.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // Endpoint consumido por el cliente Feign de users-service
    @PostMapping("/internal/create")
    public ResponseEntity<Account> createAccount(@RequestParam("userId") Long userId) {
        Account newAccount = accountService.createAccount(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(newAccount);
    }

    @GetMapping("/internal/user/{userId}")
    public ResponseEntity<Account> getByUserId(@PathVariable("userId") Long userId) {
        Optional<Account> acc = accountService.findByUserId(userId);
        return acc.map(a -> ResponseEntity.ok(a))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

}