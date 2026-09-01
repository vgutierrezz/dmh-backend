package com.dmh.users.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Captura errores de validación (@Valid) de datos obligatorios y formatos (Bad Request 400)
    // Devuelve el contrato esperado por el frontend: { "types": { field: message }, "message": "Validation failed" }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> types = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            types.put(fieldName, errorMessage);
        });
        Map<String, Object> body = new HashMap<>();
        body.put("types", types);
        body.put("message", "Validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // Captura errores de lógica de negocio como Email o DNI duplicados
    // Devuelve { "types": { "email": "..." }, "message": "Conflict" } con 409 Conflict cuando aplique
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Conflict";
        Map<String, String> types = new HashMap<>();
        String lower = msg.toLowerCase(Locale.ROOT);
        if (lower.contains("email")) {
            types.put("email", msg);
        } else if (lower.contains("dni")) {
            types.put("dni", msg);
        } else {
            types.put("error", msg);
        }
        Map<String, Object> body = new HashMap<>();
        body.put("types", types);
        body.put("message", "Conflict");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // Captura cualquier otro error inesperado en el sistema (Internal Server Error 500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllUncaughtExceptions(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        Map<String, String> types = new HashMap<>();
        types.put("error", "An unexpected internal server error occurred");
        body.put("types", types);
        body.put("message", "Internal Server Error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}