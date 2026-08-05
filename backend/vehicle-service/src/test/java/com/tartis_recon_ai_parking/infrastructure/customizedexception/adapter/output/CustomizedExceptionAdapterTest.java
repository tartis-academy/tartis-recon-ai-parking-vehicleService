package com.tartis_recon_ai_parking.infrastructure.customizedexception.adapter.output;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.tartis_recon_ai_parking.domain.vehicle.exception.ExistingVehicleException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.InvalidVehicleException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleNotFoundException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleConcurrentModificationException;
import com.tartis_recon_ai_parking.infrastructure.customizedexception.adapter.output.dto.ErrorResponse;

class CustomizedExceptionAdapterTest {

    private CustomizedExceptionAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CustomizedExceptionAdapter();
    }

    @Test
    void testHandleNotFound() {
        VehicleNotFoundException ex = new VehicleNotFoundException("Plate", "1234ABC");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/v1/vehicles");

        ResponseEntity<ErrorResponse> response = adapter.handleNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().status());
        assertEquals("NOT_FOUND", response.getBody().error());
        assertEquals(ex.getMessage(), response.getBody().message());
        assertEquals("/v1/vehicles", response.getBody().path());
    }

    @Test
    void testHandleInvalid() {
        InvalidVehicleException ex = new InvalidVehicleException("Plate", "123");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/v1/vehicles");

        ResponseEntity<ErrorResponse> response = adapter.handleInvalid(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("BAD_REQUEST", response.getBody().error());
        assertEquals(ex.getMessage(), response.getBody().message());
    }

    @Test
    void testHandleExisting() {
        ExistingVehicleException ex = new ExistingVehicleException("1234ABC");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/v1/vehicles");

        ResponseEntity<ErrorResponse> response = adapter.handleExisting(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("CONFLICT", response.getBody().error());
        assertEquals(ex.getMessage(), response.getBody().message());
    }

    @Test
    void testHandleValidation() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "field", "default message");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/v1/vehicles");

        ResponseEntity<ErrorResponse> response = adapter.handleValidation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("BAD_REQUEST", response.getBody().error());
        assertEquals("Validation failed: field: default message", response.getBody().message());
    }

    @Test
    void testHandleValidationWithoutFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/v1/vehicles");

        ResponseEntity<ErrorResponse> response = adapter.handleValidation(ex, request);

        assertEquals("BAD_REQUEST", response.getBody().error());
        assertEquals("Validation failed", response.getBody().message());
    }

    @Test
    void testHandleDataIntegrityViolationException() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Constraint violation");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/v1/vehicles");

        ResponseEntity<ErrorResponse> response = adapter.handleDataIntegrityViolationException(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("CONFLICT", response.getBody().error());
        assertEquals("The operation violates database constraints or uniqueness requirements.", response.getBody().message());
        assertEquals("/v1/vehicles", response.getBody().path());
    }

    @Test
    void testHandleDatabaseTimeoutAndConnectionErrors() {
        DataAccessResourceFailureException ex = new DataAccessResourceFailureException("DB down");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/v1/vehicles");

        ResponseEntity<ErrorResponse> response = adapter.handleDatabaseTimeoutAndConnectionErrors(ex, request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("SERVICE_UNAVAILABLE", response.getBody().error());
        assertEquals("The database is unreachable or the operation timed out. Please try again later.", response.getBody().message());
        assertEquals("/v1/vehicles", response.getBody().path());
    }

    @Test
    void testHandleCannotAcquireLockException() {
        CannotAcquireLockException ex = new CannotAcquireLockException("Deadlock found");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/v1/vehicles/1");

        ResponseEntity<ErrorResponse> response = adapter.handleCannotAcquireLockException(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("CONFLICT", response.getBody().error());
        assertEquals("The resource is currently locked by another ongoing transaction. Please retry the operation.", response.getBody().message());
        assertEquals("/v1/vehicles/1", response.getBody().path());
    }

    @Test
    void testHandleConcurrentModification() {
        VehicleConcurrentModificationException ex = new VehicleConcurrentModificationException("1234ABC",
                new ObjectOptimisticLockingFailureException("VehicleEntity", "1234ABC"));
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/v1/vehicles");

        ResponseEntity<ErrorResponse> response = adapter.handleConcurrentModification(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("CONFLICT", response.getBody().error());
        assertEquals("Vehicle with plate '1234ABC' was modified by another transaction. Please refresh and try again.", response.getBody().message());
        assertEquals("/v1/vehicles", response.getBody().path());
    }

    @Test
    void testHandleOptimisticLocking() {
        // El subtipo ObjectOptimisticLockingFailureException entra por el handler del
        // supertipo: es justo lo que evita tener que declarar los dos en @ExceptionHandler.
        OptimisticLockingFailureException ex = new ObjectOptimisticLockingFailureException("VehicleEntity", "1234ABC");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/v1/vehicles/1");

        ResponseEntity<ErrorResponse> response = adapter.handleOptimisticLocking(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("CONFLICT", response.getBody().error());
        assertEquals("The resource was modified by another transaction. Please fetch the latest version and retry.", response.getBody().message());
        assertEquals("/v1/vehicles/1", response.getBody().path());
    }

    @Test
    void testHandleGenericDatabaseException() {
        DataAccessException ex = new DataAccessException("Unknown DB error") {};
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/v1/vehicles");

        ResponseEntity<ErrorResponse> response = adapter.handleGenericDatabaseException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().error());
        assertEquals("An unexpected database failure occurred. The request could not be processed.", response.getBody().message());
        assertEquals("/v1/vehicles", response.getBody().path());
    }

    @Test
    void testHandleUnauthorized() {
        org.springframework.security.authentication.BadCredentialsException ex =
                new org.springframework.security.authentication.BadCredentialsException("Invalid token");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/v1/vehicles");

        ResponseEntity<ErrorResponse> response = adapter.handleUnauthorized(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("UNAUTHORIZED", response.getBody().error());
        assertEquals("Authentication token is missing, invalid, or expired.", response.getBody().message());
        assertEquals("/v1/vehicles", response.getBody().path());
    }

    @Test
    void testHandleAccessDenied() {
        org.springframework.security.access.AccessDeniedException ex =
                new org.springframework.security.access.AccessDeniedException("Forbidden");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/v1/vehicles");

        ResponseEntity<ErrorResponse> response = adapter.handleAccessDenied(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("FORBIDDEN", response.getBody().error());
        assertEquals("You do not have permission to perform this action.", response.getBody().message());
        assertEquals("/v1/vehicles", response.getBody().path());
    }

    @Test
    void errorResponseTimestampIsNonNull() {
        VehicleNotFoundException ex = new VehicleNotFoundException("Plate", "1234ABC");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/v1/vehicles");

        ResponseEntity<ErrorResponse> response = adapter.handleNotFound(ex, request);

        assertNotNull(response.getBody().timestamp());
    }
}