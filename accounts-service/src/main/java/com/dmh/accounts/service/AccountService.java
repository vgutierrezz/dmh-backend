package com.dmh.accounts.service;

import com.dmh.accounts.dto.*;
import com.dmh.accounts.exception.AccountNotFoundException;
import com.dmh.accounts.exception.AliasAlreadyExistsException;
import com.dmh.accounts.exception.InsufficientFundsException;
import com.dmh.accounts.exception.InvalidAmountException;
import com.dmh.accounts.model.Account;
import com.dmh.accounts.repository.AccountRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;

@Service
@AllArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final ActivityService activityService;

    private final List<String> wordDictionary = new ArrayList<>();
    private final Random random = new Random();

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    @PostConstruct
    public void loadDictionary() {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new ClassPathResource("aliases.txt").getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    wordDictionary.add(line.trim().toLowerCase());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not load alias dictionary file", e);
        }
    }

    public AccountResponse createAccount(Long userId) {
        Account account = new Account();
        account.setUserId(userId);
        account.setBalance(BigDecimal.ZERO);

        account.setCvu(generateUniqueCvu());
        account.setAlias(generateUniqueAlias());

        Account savedAccount = accountRepository.save(account);
        return new AccountResponse(
                String.valueOf(savedAccount.getId()),
                String.valueOf(savedAccount.getUserId()),
                savedAccount.getBalance(),
                savedAccount.getCvu(),
                savedAccount.getAlias()
        );
    }

    private String generateUniqueCvu() {
        String cvu;
        do {
            StringBuilder sb = new StringBuilder();
            sb.append("000000");
            for (int i = 0; i < 16; i++) {
                sb.append(random.nextInt(10));
            }
            cvu = sb.toString();
        } while (accountRepository.existsByCvu(cvu));
        return cvu;
    }

    private String generateUniqueAlias() {
        if (wordDictionary.size() < 3) {
            throw new IllegalStateException("Dictionary does not have enough words to generate an alias");
        }

        String alias;
        do {
            String p1 = wordDictionary.get(random.nextInt(wordDictionary.size()));
            String p2 = wordDictionary.get(random.nextInt(wordDictionary.size()));
            String p3 = wordDictionary.get(random.nextInt(wordDictionary.size()));

            while (p1.equals(p2)) p2 = wordDictionary.get(random.nextInt(wordDictionary.size()));
            while (p3.equals(p1) || p3.equals(p2)) p3 = wordDictionary.get(random.nextInt(wordDictionary.size()));

            alias = String.format("%s.%s.%s", p1, p2, p3);
        } while (accountRepository.existsByAlias(alias));
        return alias;
    }

    public Optional<Account> findByUserId(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    @Transactional
    public AccountResponse updateAlias(Long accountOrUserId, AliasUpdateRequest request) {
        if (request == null || request.alias() == null || request.alias().isBlank()) {
            throw new IllegalArgumentException("El alias es obligatorio");
        }

        String alias = normalizeAlias(request.alias());

        Account account = accountRepository.findByUserId(accountOrUserId)
                .or(() -> accountRepository.findById(accountOrUserId))
                .orElseThrow(() -> new AccountNotFoundException("Cuenta inexistente"));

        if (accountRepository.existsByAliasIgnoreCaseAndIdNot(alias, account.getId())) {
            throw new AliasAlreadyExistsException(alias);
        }

        account.setAlias(alias);
        Account updated = accountRepository.save(account);

        return new AccountResponse(
                String.valueOf(updated.getId()),
                String.valueOf(updated.getUserId()),
                updated.getBalance(),
                updated.getCvu(),
                updated.getAlias()
        );
    }

    private String normalizeAlias(String alias) {
        return alias.trim().toLowerCase(Locale.ROOT);
    }

    @Transactional
    public void createTransfer(Long userId, ActivityRequest transferRequest) {
        Optional<Account> accountOrigin = accountRepository.findByUserId(userId);
        Optional<Account> destinationAccount = accountRepository.findByCvu(transferRequest.destination());
        BigDecimal amount = resolveAmount(transferRequest);

        if (accountOrigin.isEmpty()) {
            throw new AccountNotFoundException("Cuenta inexistente");
        }

        if (destinationAccount.isEmpty()) {
            throw new AccountNotFoundException("Cuenta inexistente");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("El monto debe ser mayor a cero");
        }

        if (accountOrigin.get().getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Fondos insuficientes");
        }

        accountOrigin.get().setBalance(accountOrigin.get().getBalance().subtract(amount));
        destinationAccount.get().setBalance(destinationAccount.get().getBalance().add(amount));

        accountRepository.save(accountOrigin.get());
        accountRepository.save(destinationAccount.get());

        activityService.createActivity(
                destinationAccount.get().getId().toString(),
                amount,
                accountOrigin.get().getId(),
                "CREDIT",
                transferRequest.type(),
                accountOrigin.get().getCvu()
        );
    }
    private BigDecimal resolveAmount(ActivityRequest transferRequest) {
        if (transferRequest.amount() == null) {
            throw new InvalidAmountException("El monto de la transferencia es obligatorio");
        }
        return transferRequest.amount().abs();
    }

    @Transactional
    public ActivityResponse deposit(
            Long userId,
            DepositRequest request
    ) {

        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Cuenta inexistente para el usuario: "
                                        + userId
                        )
                );

        if (request == null || request.amount() == null) {
            throw new InvalidAmountException(
                    "El monto del depósito es obligatorio"
            );
        }

        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(
                    "El monto del depósito debe ser mayor a cero"
            );
        }

        if (request.type() == null
                || !request.type().equalsIgnoreCase("Deposit")) {

            throw new IllegalArgumentException(
                    "El tipo de operación debe ser Deposit"
            );
        }

        String description =
                request.description() == null
                        || request.description().isBlank()
                        ? "Depósito con tarjeta"
                        : request.description().trim();

        BigDecimal updatedBalance =
                account.getBalance().add(request.amount());

        account.setBalance(updatedBalance);
        accountRepository.save(account);

        return activityService.createActivity(
                "Tarjeta",
                request.amount(),
                account.getId(),
                description,
                "DEPOSIT",
                account.getCvu()
        );
    }
}