package com.btg.fondos.infrastructure;

import com.btg.fondos.application.dto.ErrorResponse;
import com.btg.fondos.domain.exception.AlreadySubscribedException;
import com.btg.fondos.domain.exception.InsufficientBalanceException;
import com.btg.fondos.domain.exception.NotSubscribedException;
import com.btg.fondos.domain.exception.ResourceNotFoundException;
import com.btg.fondos.infrastructure.adapter.in.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleInsufficientBalance() {
        ResponseEntity<ErrorResponse> response = handler.handleInsufficientBalance(
                new InsufficientBalanceException("FPV_BTG"));

        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().message().contains("FPV_BTG"));
    }

    @Test
    void handleAlreadySubscribed() {
        ResponseEntity<ErrorResponse> response = handler.handleAlreadySubscribed(
                new AlreadySubscribedException("FPV_BTG"));

        assertEquals(409, response.getStatusCode().value());
        assertTrue(response.getBody().message().contains("Ya se encuentra suscrito"));
    }

    @Test
    void handleNotSubscribed() {
        ResponseEntity<ErrorResponse> response = handler.handleNotSubscribed(
                new NotSubscribedException("FPV_BTG"));

        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().message().contains("No se encuentra suscrito"));
    }

    @Test
    void handleNotFound() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(
                new ResourceNotFoundException("No encontrado"));

        assertEquals(404, response.getStatusCode().value());
        assertEquals("No encontrado", response.getBody().message());
    }

    @Test
    void handleIllegalArgument() {
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(
                new IllegalArgumentException("Ya existe"));

        assertEquals(409, response.getStatusCode().value());
        assertEquals("Ya existe", response.getBody().message());
    }

    @Test
    void handleGeneral() {
        ResponseEntity<ErrorResponse> response = handler.handleGeneral(new RuntimeException("error"));

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Error interno del servidor", response.getBody().message());
    }
}
