package com.tartis_recon_ai_parking.domain.vehicle.exception;

public class VehicleConcurrentModificationException extends RuntimeException {

    private final String plate;

    public VehicleConcurrentModificationException(String plate, Throwable cause) {
        super("Vehicle with plate '" + plate + "' was modified by another transaction. Please refresh and try again.", cause);
        this.plate = plate;
    }

    public String getPlate() {
        return this.plate;
    }
}
