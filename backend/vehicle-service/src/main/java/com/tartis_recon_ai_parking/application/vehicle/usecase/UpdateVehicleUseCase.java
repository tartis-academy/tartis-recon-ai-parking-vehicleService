package com.tartis_recon_ai_parking.application.vehicle.usecase;

import java.util.UUID;

import com.tartis_recon_ai_parking.application.vehicle.dto.VehicleCreateDTO;
import com.tartis_recon_ai_parking.application.vehicle.dto.VehicleDTO;
import com.tartis_recon_ai_parking.application.vehicle.factory.VehicleDTOFactory;
import com.tartis_recon_ai_parking.application.vehicle.port.output.VehiclePersistence;
import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.domain.vehicle.exception.InvalidVehicleException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleNotFoundException;

public class UpdateVehicleUseCase {

    private final VehiclePersistence vehiclePersistence;

    public UpdateVehicleUseCase(VehiclePersistence vehiclePersistence) {
        this.vehiclePersistence = vehiclePersistence;
    }

    /**
     * El id llega como parametro y no dentro del DTO: identifica el recurso de la
     * URL, no es un dato que el cliente pueda modificar en el cuerpo.
     */
    public VehicleDTO execute(UUID id, VehicleCreateDTO updatedData)
            throws VehicleNotFoundException, InvalidVehicleException {

        // 1. Validamos los datos entrantes ANTES de ir a la BD: si el cuerpo es
        //    invalido debe ganar ese error, no el 404 de un id inexistente.
        Vehicle newData = VehicleDTOFactory.toDomain(updatedData);

        // 2. Recuperamos el vehiculo existente.
        Vehicle existingVehicle = vehiclePersistence.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found with ID: " + id));

        // 3. Modificamos los atributos a traves de los setters con validacion.
        //    setType va primero: setHasSidecar valida contra el tipo ya asignado.
        existingVehicle.setType(newData.getType());
        existingVehicle.setPlate(newData.getPlate());
        existingVehicle.setBrand(newData.getBrand());
        existingVehicle.setModel(newData.getModel());
        existingVehicle.setColor(newData.getColor());
        existingVehicle.setNumDoors(newData.getNumDoors());
        existingVehicle.setHasSidecar(newData.getHasSidecar());
        existingVehicle.setActive(newData.isActive());

        // 4. Guardamos los cambios.
        return VehicleDTOFactory.toDTO(vehiclePersistence.save(existingVehicle));
    }
}
