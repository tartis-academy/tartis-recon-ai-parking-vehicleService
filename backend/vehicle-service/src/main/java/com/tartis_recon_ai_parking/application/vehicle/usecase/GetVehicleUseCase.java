package com.tartis_recon_ai_parking.application.vehicle.usecase;

import java.util.List;
import com.tartis_recon_ai_parking.application.vehicle.port.output.VehiclePersistence;
import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleNotFoundException;

public class GetVehicleUseCase {
    private final VehiclePersistence persistence;

    public GetVehicleUseCase(VehiclePersistence persistence) {
        this.persistence = persistence;
    }

    public Vehicle getByPlate(String plate) throws VehicleNotFoundException {
        return persistence.findByPlate(plate)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle with plate " + plate + " not found"));
    }

    public Vehicle getById(java.util.UUID id) throws VehicleNotFoundException {
        return persistence.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle with ID " + id + " not found"));
    }

    public List<Vehicle> execute() {
        return persistence.findAll();
    }
}