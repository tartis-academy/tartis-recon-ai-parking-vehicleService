package com.tartis_recon_ai_parking.application.vehicle.usecase;

import com.tartis_recon_ai_parking.application.vehicle.dto.VehicleDTO;
import com.tartis_recon_ai_parking.application.vehicle.factory.VehicleDTOFactory;
import com.tartis_recon_ai_parking.application.vehicle.port.output.VehiclePersistence;
import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Transactional
public class ActivateVehicleUseCase {

    private final VehiclePersistence persistence;

    public ActivateVehicleUseCase(VehiclePersistence persistence) {
        this.persistence = persistence;
    }

    /**
     * Activa el vehiculo identificado por su id de forma idempotente.
     *
     * @param id identificador unico del vehiculo
     * @return VehicleDTO con el estado actualizado
     * @throws VehicleNotFoundException si no existe ningun vehiculo con ese id
     */
    public VehicleDTO activate(UUID id) {
        Vehicle vehicle = persistence.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException("ID", id));

        vehicle = vehicle.activate();
        Vehicle savedVehicle = persistence.save(vehicle);
        return VehicleDTOFactory.toDTO(savedVehicle);
    }
}
