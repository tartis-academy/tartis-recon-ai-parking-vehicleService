package com.tartis.parking.application.vehicle.usecase;

import com.tartis.parking.domain.vehicle.model.Vehicle;
import com.tartis.parking.domain.vehicle.exception.InvalidVehicleException;
import com.tartis.parking.application.vehicle.port.output.VehiclePersistence;

public class CreateVehicleUseCase {

    private final VehiclePersistence vehiclePersistence;

    public CreateVehicleUseCase(VehiclePersistence vehiclePersistence) {
        this.vehiclePersistence = vehiclePersistence;
    }

    public Vehicle execute(Vehicle vehicle) {
        // Validar que no exista otro vehículo con la misma matrícula
        if (vehiclePersistence.existsByPlate(vehicle.getPlate())) {
            throw new InvalidVehicleException("Ya existe un vehículo con la matrícula: " + vehicle.getPlate());
        }
        return vehiclePersistence.save(vehicle);
    }
}