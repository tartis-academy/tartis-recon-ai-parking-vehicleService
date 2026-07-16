package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class VehicleRequest {
    
    @NotBlank(message = "El tipo es obligatorio")
    public String type;
    
    @NotBlank(message = "La matrícula es obligatoria")
    public String plate;
    
    public String brand;
    public String model;
    public String color;
    public Integer numDoors;
    public Boolean hasSidecar;
    
    @NotNull(message = "El estado activo es obligatorio")
    public Boolean active;
}
