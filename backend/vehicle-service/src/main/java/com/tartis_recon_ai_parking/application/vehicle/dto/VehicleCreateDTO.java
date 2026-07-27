package com.tartis_recon_ai_parking.application.vehicle.dto;

public record VehicleCreateDTO(
        String type,
        String plate,
        String brand,
        String model,
        String color,
        Integer numDoors,
        Boolean hasSidecar) {
}
