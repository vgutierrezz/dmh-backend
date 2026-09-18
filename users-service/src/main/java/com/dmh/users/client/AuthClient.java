package com.dmh.users.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "auth-service")
public interface AuthClient {

    @PostMapping("/api/auth/login")
    Map<String, String> login(@RequestBody Map<String, String> credentials);
}
