package com.tartis_recon_ai_parking.application.vehicle.usecase;

import org.springframework.stereotype.Component;

import com.tartis_recon_ai_parking.application.vehicle.port.output.VehiclePersistence;
import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleNotFoundException;



@Component
public class GetVehicleUseCase {
    private final VehiclePersistence persistence;

    public GetVehicleUseCase(VehiclePersistence persistence) {
        this.persistence = persistence;
    }

    public Vehicle getByPlate(String plate) throws VehicleNotFoundException {
        return persistence.findByPlate(plate)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle with plate " + plate + " not found"));
    }
}
