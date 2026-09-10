package com.dmh.accounts.service;

import com.dmh.accounts.dto.AccountResponse;
import com.dmh.accounts.dto.ActivityRequest;
import com.dmh.accounts.dto.ActivityResponse;
import com.dmh.accounts.exception.AccountNotFoundException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

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

    // Carga el archivo aliases.txt en memoria al iniciar el servicio
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
        account.setBalance(BigDecimal.ZERO); // Inicializa con saldo en cero

        // Generar CVU y Alias únicos asegurando que no colisionen en la base de datos
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

    // Algoritmo para generar un CVU aleatorio de 22 dígitos
    private String generateUniqueCvu() {
        String cvu;
        do {
            StringBuilder sb = new StringBuilder();
            // Los primeros dígitos en Argentina suelen identificar al banco/proveedor, el resto es la cuenta
            sb.append("000000"); // Prefijo fijo simulado de PSP digital
            for (int i = 0; i < 16; i++) {
                sb.append(random.nextInt(10));
            }
            cvu = sb.toString();
        } while (accountRepository.existsByCvu(cvu)); // Si ya existe, genera otro
        return cvu;
    }

    // Algoritmo para generar un Alias aleatorio de 3 palabras (palabra1.palabra2.palabra3)
    private String generateUniqueAlias() {
        if (wordDictionary.size() < 3) {
            throw new IllegalStateException("Dictionary does not have enough words to generate an alias");
        }

        String alias;
        do {
            String p1 = wordDictionary.get(random.nextInt(wordDictionary.size()));
            String p2 = wordDictionary.get(random.nextInt(wordDictionary.size()));
            String p3 = wordDictionary.get(random.nextInt(wordDictionary.size()));

            // Asegurar que las 3 palabras sean distintas para que quede más prolijo
            while (p1.equals(p2)) p2 = wordDictionary.get(random.nextInt(wordDictionary.size()));
            while (p3.equals(p1) || p3.equals(p2)) p3 = wordDictionary.get(random.nextInt(wordDictionary.size()));

            alias = String.format("%s.%s.%s", p1, p2, p3);
        } while (accountRepository.existsByAlias(alias)); // Si ya existe, genera otro
        return alias;
    }

    public Optional<Account> findByUserId(Long userId) {
        return accountRepository.findByUserId(userId);
    }


    @Transactional
    public ActivityResponse createTransfer(Long userId, ActivityRequest transferRequest) {
        Optional<Account> accountOrigin = accountRepository.findByUserId(userId);
        Optional<Account> destinationAccount = accountRepository.findByCvu(transferRequest.destination());
        BigDecimal amount = transferRequest.amount().abs();

        if (accountOrigin.isEmpty()) {
            throw new AccountNotFoundException("Cuenta inexistente");
        }

        if (destinationAccount.isEmpty()) {
            throw new AccountNotFoundException("Cuenta inexistente");
        }

        if (amount == null) {
            throw new InvalidAmountException("El monto de la transferencia es obligatorio");
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

        return activityService.createActivity(
                accountOrigin.get().getId().toString(),
                amount,
                destinationAccount.get().getId(),
                "DEBIT",
                transferRequest.type(),
                transferRequest.destination()
        );
    }

}