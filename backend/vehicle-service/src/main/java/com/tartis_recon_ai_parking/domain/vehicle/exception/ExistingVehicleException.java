package com.tartis_recon_ai_parking.domain.vehicle.exception;


public class ExistingVehicleException extends RuntimeException {
    
    private final String plate;

    public ExistingVehicleException(String plate) {
        super("There's already a vehicle with the specified plate : " + plate);
        this.plate = plate;
    }

    // Conserva la excepcion original (violacion de la constraint) como causa,
    // para no perder el rastro al traducir en el adaptador de persistencia.
    public ExistingVehicleException(String plate, Throwable cause) {
        super("There's already a vehicle with the specified plate : " + plate, cause);
        this.plate = plate;
    }

    public String getPlate(){
        return this.plate;
    }

}
