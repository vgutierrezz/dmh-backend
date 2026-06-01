package com.dmh.users.client;

import com.dmh.users.dto.AccountResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "accounts-service") // Nombre lógico registrado en Eureka
public interface AccountClient {

    // Este endpoint lo crearemos en el accounts-service para inicializar la billetera
    @PostMapping("/accounts/internal/create")
    AccountResponse createAccount(@RequestParam("userId") Long userId);
}