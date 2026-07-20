package com.tartis_recon_ai_parking.application.vehicle.usecase;

import com.tartis_recon_ai_parking.application.vehicle.port.output.VehiclePersistence;
import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleNotFoundException;
import java.util.UUID;

public class DeleteVehicleUseCase {

    private final VehiclePersistence persistence;

    public DeleteVehicleUseCase(VehiclePersistence persistence) {
        this.persistence = persistence;
    }

    /**
     * Desactiva el vehiculo identificado por su id.
     *
     * @param id identificador unico del vehiculo
     * @throws VehicleNotFoundException si no existe ningun vehiculo con ese id (CA7)
     */
    public void deactivate(UUID id) throws VehicleNotFoundException {

        // 1. Recuperamos el vehiculo a traves del puerto de salida.
        Vehicle vehicle = persistence.findById(id)
        .orElseThrow(() -> new VehicleNotFoundException("Vehículo no encontrado con id: " + id));

        // 2. El dominio aplica el cambio de estado.
        vehicle = vehicle.deactivate();

        // 3. Persistimos. Sin este save() el cambio se queda en memoria:
        //    el objeto de dominio NO es una entidad gestionada por JPA,
        //    sale de un mapper, asi que no hay dirty checking.
        persistence.save(vehicle);
    }
}