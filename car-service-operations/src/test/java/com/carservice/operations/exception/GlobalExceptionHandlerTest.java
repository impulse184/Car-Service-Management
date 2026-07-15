package com.carservice.operations.exception;

import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    public void handleValidationExceptions_HappyPath_ReturnsFieldValidationErrorMap() {
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        FieldError error = new FieldError("dto", "carRegistrationNumber", "Registration number is mandatory");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(error));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("message").toString().contains("carRegistrationNumber: Registration number is mandatory"));
    }

    @Test
    public void handleValidationExceptions_EmptyPath_ReturnsEmptyErrorMapWhenNoFields() {
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.emptyList());
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed: []", response.getBody().get("message"));
    }

    @Test
    public void handleIllegalArgumentExceptions_HappyPath_ReturnsBadRequestWithErrorDetails() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid input format");

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgumentExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid input format", response.getBody().get("message"));
    }

    @Test
    public void handleIllegalArgumentExceptions_AlternativePath_HandlesGenericOrEmptyMessages() {
        IllegalArgumentException ex = new IllegalArgumentException((String) null);

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgumentExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("", response.getBody().get("message"));
    }

    @Test
    public void handleFeignStatusException_HappyPath_ReturnsBadGatewayPayload() {
        Request request = Request.create(Request.HttpMethod.GET, "/validate", Collections.emptyMap(), (byte[]) null, (java.nio.charset.Charset) null);
        FeignException.InternalServerError ex = new FeignException.InternalServerError("Validation service error", request, new byte[0], null);

        ResponseEntity<Map<String, Object>> response = handler.handleFeignStatusException(ex);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("The validation microservice was unreachable.", response.getBody().get("message"));
    }

    @Test
    public void handleFeignStatusException_AlternativePath_ProcessesDifferentFeignExceptionsUniformly() {
        Request request = Request.create(Request.HttpMethod.POST, "/validate", Collections.emptyMap(), (byte[]) null, (java.nio.charset.Charset) null);
        FeignException.NotFound ex = new FeignException.NotFound("Service endpoint not found", request, new byte[0], null);

        ResponseEntity<Map<String, Object>> response = handler.handleFeignStatusException(ex);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("The validation microservice was unreachable.", response.getBody().get("message"));
    }

    @Test
    public void handleRuntimeExceptions_HappyPath_ReturnsNotFoundStatusWithExceptionMessage() {
        RuntimeException ex = new RuntimeException("Service record not found");

        ResponseEntity<Map<String, Object>> response = handler.handleRuntimeExceptions(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Service record not found", response.getBody().get("message"));
    }

    @Test
    public void handleRuntimeExceptions_AlternativePath_HandlesGenericUserNotFoundExceptions() {
        RuntimeException ex = new RuntimeException("Record not found");

        ResponseEntity<Map<String, Object>> response = handler.handleRuntimeExceptions(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Record not found", response.getBody().get("message"));
    }
}
