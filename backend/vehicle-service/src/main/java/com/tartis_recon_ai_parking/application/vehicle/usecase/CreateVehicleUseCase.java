package com.tartis_recon_ai_parking.application.vehicle.usecase;

import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.domain.vehicle.exception.InvalidVehicleException;
import com.tartis_recon_ai_parking.application.vehicle.port.output.VehiclePersistence;

public class CreateVehicleUseCase {

    private final VehiclePersistence vehiclePersistence;

    public CreateVehicleUseCase(VehiclePersistence vehiclePersistence) {
        this.vehiclePersistence = vehiclePersistence;
    }

    public Vehicle execute(Vehicle vehicle) {
        if (vehiclePersistence.existsByPlate(vehicle.getPlate())) {
            throw new InvalidVehicleException("Ya existe un vehículo con la matrícula: " + vehicle.getPlate());
        }
        return vehiclePersistence.save(vehicle);
    }
}
