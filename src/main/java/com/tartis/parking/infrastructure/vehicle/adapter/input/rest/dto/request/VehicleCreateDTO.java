package com.tartis.parking.infrastructure.vehicle.adapter.input.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public class VehicleCreateDTO {
    @NotBlank(message = "El tipo es obligatorio")
    public String type;
    
    @NotBlank(message = "La matrícula es obligatoria")
    public String plate;
    
    public String brand;
    public String model;
    public String color;
    public Integer numDoors;
    public Boolean hasSidecar;
}