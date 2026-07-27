package com.tartis_recon_ai_parking.domain.vehicle.exception;


public class ExistingVehicleException extends RuntimeException {
    
    private final String plate;

    public ExistingVehicleException(String plate) {
        super("There's already a vehicle with the specified plate : " + plate);
        this.plate = plate;
    }

    public String getPlate(){
        return this.plate;
    }

}