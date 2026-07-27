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
        assertEquals("field: default message", response.getProperties().get("errors"));
    }
}
