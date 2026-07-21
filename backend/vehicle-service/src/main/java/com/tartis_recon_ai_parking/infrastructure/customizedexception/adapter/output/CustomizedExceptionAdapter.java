package com.tartis_recon_ai_parking.infrastructure.customizedexception.adapter.output;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.tartis_recon_ai_parking.domain.vehicle.exception.InvalidVehicleException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleNotFoundException;

@RestControllerAdvice
public class CustomizedExceptionAdapter {

    @ExceptionHandler(VehicleNotFoundException.class)
    public ResponseEntity<String> handleNotFound(VehicleNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidVehicleException.class)
    public ResponseEntity<String> handleInvalid(InvalidVehicleException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
