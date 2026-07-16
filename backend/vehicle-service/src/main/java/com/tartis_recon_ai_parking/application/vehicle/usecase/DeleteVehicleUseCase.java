package com.tartis_recon_ai_parking.application.vehicle.usecase;

import com.tartis_recon_ai_parking.application.vehicle.port.output.VehiclePersistence;
import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleNotFoundException;

/**
 * Caso de uso de baja logica de un vehiculo (CA6 de HU-06, regla RN-11).
 *
 * Marca el vehiculo como inactivo para impedir su entrada al aparcamiento.
 * No elimina el registro de la base de datos.
 *
 * Esta clase orquesta, no decide: no conoce Spring, ni JPA, ni HTTP.
 * Solo depende del puerto de salida VehiclePersistence, que es una
 * interfaz de la propia capa de aplicacion.
 */
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
    public void deactivate(Long id) throws VehicleNotFoundException {

        // 1. Recuperamos el vehiculo a traves del puerto de salida.
        Vehicle vehicle = persistence.findById(id)
        .orElseThrow(() -> new VehicleNotFoundException("Vehículo no encontrado con id: " + id));

        // 2. El dominio aplica el cambio de estado.
        vehicle.setActive(false);

        // 3. Persistimos. Sin este save() el cambio se queda en memoria:
        //    el objeto de dominio NO es una entidad gestionada por JPA,
        //    sale de un mapper, asi que no hay dirty checking.
        persistence.save(vehicle);
    }
}