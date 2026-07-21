package com.tartis_recon_ai_parking.domain.vehicle.exception;

// Cambia "extends Exception" por "extends RuntimeException"
public class InvalidVehicleException extends RuntimeException {
    public InvalidVehicleException(String message) {
        super(message);
    }
}