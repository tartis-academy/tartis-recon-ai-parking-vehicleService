package com.tartis_recon_ai_parking.application.vehicle.dto;

import java.time.Instant;
import java.util.UUID;

import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.domain.vehicle.VehicleType;

public record VehicleChangedEvent(
    UUID eventId,
    String type,
    String version,
    Instant occurredAt,
    VehicleChangedData data
) {
    @SuppressWarnings("java:S107")
    public record VehicleChangedData(
        UUID vehicleId,
        String plate,
        VehicleType vehicleType,
        String brand,
        String model,
        String color,
        int numDoors,
        boolean hasSidecar,
        boolean active
    ) {}

    public static VehicleChangedEvent of(Vehicle vehicle, Instant occurredAt) {
        return new VehicleChangedEvent(
            UUID.randomUUID(), "VehicleChangedEvent", "v1", occurredAt,
            new VehicleChangedData(vehicle.getUniqueId(), vehicle.getPlate(), vehicle.getType(),
                vehicle.getBrand(), vehicle.getModel(), vehicle.getColor(),
                vehicle.getNumDoors(), vehicle.getHasSidecar(), vehicle.isActive())
        );
    }
}
