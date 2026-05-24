package com.example.finance.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void handleNotFound() {
        ResponseEntity<Map<String, String>> response = handler.handleNotFound(new NotFoundException("not found"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("not found", response.getBody().get("message"));
    }

    @Test
    void handleBadRequest() {
        ResponseEntity<Map<String, String>> response = handler.handleBadRequest(new BadRequestException("bad request"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("bad request", response.getBody().get("message"));
    }

    @Test
    void handleConflict() {
        ResponseEntity<Map<String, String>> response = handler.handleConflict(new ConflictException("conflict"));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("conflict", response.getBody().get("message"));
    }

    @Test
    void handleUnauthorized() {
        ResponseEntity<Map<String, String>> response = handler.handleUnauthorized(new BadCredentialsException("bad creds"));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid credentials or session expired", response.getBody().get("message"));
    }

    @Test
    void handleValidation() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "field", "defaultMessage");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        ResponseEntity<Map<String, String>> response = handler.handleValidation(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("defaultMessage", response.getBody().get("field"));
    }

    @Test
    void handleMessageNotReadable_categoryType() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("some message containing CategoryType");
        ResponseEntity<Map<String, String>> response = handler.handleMessageNotReadable(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid category type. Allowed values: INCOME, EXPENSE", response.getBody().get("message"));
    }

    @Test
    void handleMessageNotReadable_general() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("some message");
        ResponseEntity<Map<String, String>> response = handler.handleMessageNotReadable(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid request body", response.getBody().get("message"));
    }

    @Test
    void handleGeneral() {
        ResponseEntity<Map<String, String>> response = handler.handleGeneral(new RuntimeException("general error"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("general error", response.getBody().get("message"));
    }
}
