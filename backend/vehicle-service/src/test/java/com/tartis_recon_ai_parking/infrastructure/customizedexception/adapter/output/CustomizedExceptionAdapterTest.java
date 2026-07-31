package com.tartis_recon_ai_parking.infrastructure.customizedexception.adapter.output;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessException;

import com.tartis_recon_ai_parking.domain.vehicle.exception.ExistingVehicleException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.InvalidVehicleException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleNotFoundException;

class CustomizedExceptionAdapterTest {

    private CustomizedExceptionAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CustomizedExceptionAdapter();
    }

    @Test
    void testHandleNotFound() {
        VehicleNotFoundException ex = new VehicleNotFoundException("Plate", "1234ABC");
        ProblemDetail response = adapter.handleNotFound(ex);
        
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
        assertEquals(ex.getMessage(), response.getDetail());
    }

    @Test
    void testHandleInvalid() {
        InvalidVehicleException ex = new InvalidVehicleException("Plate", "123");
        ProblemDetail response = adapter.handleInvalid(ex);
        
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        assertEquals(ex.getMessage(), response.getDetail());
    }

    @Test
    void testHandleExisting() {
        ExistingVehicleException ex = new ExistingVehicleException("1234ABC");
        ProblemDetail response = adapter.handleExisting(ex);
        
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        assertEquals(ex.getMessage(), response.getDetail());
    }

    @Test
    void testHandleValidation() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "field", "default message");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ProblemDetail response = adapter.handleValidation(ex);
        
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        assertEquals("Validation failed", response.getDetail());
        assertNotNull(response.getProperties());
        assertEquals("field: default message", response.getProperties().get("errors"));
    }

    @Test
    void testHandleDataIntegrityViolationException() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Constraint violation");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/v1/vehicles");

        ProblemDetail response = adapter.handleDataIntegrityViolationException(ex, request);
        
        assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());
        assertEquals("Data Integrity Violation", response.getTitle());
        assertEquals("The operation violates database constraints or uniqueness requirements.", response.getDetail());
        assertEquals(java.net.URI.create("/v1/vehicles"), response.getInstance());
    }

    @Test
    void testHandleDatabaseTimeoutAndConnectionErrors() {
        DataAccessResourceFailureException ex = new DataAccessResourceFailureException("DB down");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/v1/vehicles");

        ProblemDetail response = adapter.handleDatabaseTimeoutAndConnectionErrors(ex, request);
        
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), response.getStatus());
        assertEquals("Database Service Unavailable", response.getTitle());
        assertEquals("The database is unreachable or the operation timed out. Please try again later.", response.getDetail());
        assertEquals(java.net.URI.create("/v1/vehicles"), response.getInstance());
    }

    @Test
    void testHandleCannotAcquireLockException() {
        CannotAcquireLockException ex = new CannotAcquireLockException("Deadlock found");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/v1/vehicles/1");

        ProblemDetail response = adapter.handleCannotAcquireLockException(ex, request);
        
        assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());
        assertEquals("Concurrency Lock Conflict", response.getTitle());
        assertEquals("The resource is currently locked by another ongoing transaction. Please retry the operation.", response.getDetail());
        assertEquals(java.net.URI.create("/v1/vehicles/1"), response.getInstance());
    }

    @Test
    void testHandleGenericDatabaseException() {
        DataAccessException ex = new DataAccessException("Unknown DB error") {};
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/v1/vehicles");

        ProblemDetail response = adapter.handleGenericDatabaseException(ex, request);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
        assertEquals("Internal Database Error", response.getTitle());
        assertEquals("An unexpected database failure occurred. The request could not be processed.", response.getDetail());
        assertEquals(java.net.URI.create("/v1/vehicles"), response.getInstance());
    }

    @Test
    void testHandleUnauthorized() {
        org.springframework.security.authentication.BadCredentialsException ex =
                new org.springframework.security.authentication.BadCredentialsException("Invalid token");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/v1/vehicles");

        ProblemDetail response = adapter.handleUnauthorized(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertEquals("Unauthorized Access", response.getTitle());
        assertEquals("Authentication token is missing, invalid, or expired.", response.getDetail());
        assertEquals(java.net.URI.create("/v1/vehicles"), response.getInstance());
    }

    @Test
    void testHandleAccessDenied() {
        org.springframework.security.access.AccessDeniedException ex =
                new org.springframework.security.access.AccessDeniedException("Forbidden");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/v1/vehicles");

        ProblemDetail response = adapter.handleAccessDenied(ex, request);

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
        assertEquals("Forbidden Access", response.getTitle());
        assertEquals("You do not have permission to perform this action.", response.getDetail());
        assertEquals(java.net.URI.create("/v1/vehicles"), response.getInstance());
    }
}
