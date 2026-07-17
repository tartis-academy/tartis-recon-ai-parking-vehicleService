package com.tartis_recon_ai_parking.application.vehicle.dto;

import java.util.UUID;

public record VehicleDTO(
        UUID uniqueId,
        String type,
        String plate,
        String brand,
        String model,
        String color,
        int numDoors,
        boolean hasSidecar,
        boolean active) {
}
