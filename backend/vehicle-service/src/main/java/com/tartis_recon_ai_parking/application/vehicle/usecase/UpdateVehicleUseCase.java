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

        // 3. La matricula es unique en BD. Sin esta comprobacion el choque lo
        //    detecta Postgres al hacer flush y sale como 500; aqui sale como 400.
        //    El filter es imprescindible: si el vehiculo conserva su propia
        //    matricula la encontramos a el mismo, y eso no es un duplicado.
        vehiclePersistence.findByPlate(newData.getPlate())
                .filter(other -> !other.getUniqueId().equals(id))
                .ifPresent(other -> {
                    throw new InvalidVehicleException(
                            "Ya existe un vehículo con la matrícula: " + newData.getPlate());
                });

        // 4. Modificamos los atributos. 'active' no se toca aqui: se conserva el
        //    valor actual del vehiculo; solo cambia via PATCH /{id}/status.
        Vehicle updatedVehicle = existingVehicle.update(newData.getType(), newData.getPlate(), newData.getBrand(), newData.getModel(),
            newData.getColor(), newData.getNumDoors(), newData.getHasSidecar(), existingVehicle.isActive());

        // 5. Guardamos los cambios.
        return VehicleDTOFactory.toDTO(vehiclePersistence.save(updatedVehicle));
    }
}
