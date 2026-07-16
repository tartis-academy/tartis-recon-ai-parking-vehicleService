package com.tartis_recon_ai_parking.application.vehicle.usecase;

import com.tartis_recon_ai_parking.application.vehicle.port.output.VehiclePersistence;
import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.domain.vehicle.exception.InvalidVehicleException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleNotFoundException;


public class UpdateVehicleUseCase {

    private final VehiclePersistence vehiclePersistence;

    public UpdateVehicleUseCase(VehiclePersistence vehiclePersistence) {
        this.vehiclePersistence = vehiclePersistence;
    }

    public Vehicle execute(Vehicle updatedData) throws VehicleNotFoundException, InvalidVehicleException {
        // 1. Buscamos por el ID único numérico
        Vehicle existingVehicle = vehiclePersistence.findById(updatedData.getUniqueId())
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found with ID: " + updatedData.getUniqueId()));

        // 2. Modificamos los atributos a través de tus setters con validación
        existingVehicle.setType(updatedData.getType());
        existingVehicle.setPlate(updatedData.getPlate()); // Por si acaso la matrícula cambia
        existingVehicle.setBrand(updatedData.getBrand());
        existingVehicle.setModel(updatedData.getModel());
        existingVehicle.setColor(updatedData.getColor());
        existingVehicle.setNumDoors(updatedData.getNumDoors());
        existingVehicle.setSideCar(updatedData.getSideCar());
        existingVehicle.setActive(updatedData.isActive());

        // 3. Guardamos los cambios
        return vehiclePersistence.save(existingVehicle);
    }
}