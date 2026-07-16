package com.tartis_recon_ai_parking.application.vehicle.port.output;

import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import java.util.Optional;

/*
 Puerto de Salida para la persistencia de Vehicle
*/
public interface VehiclePersistence {

    // Guarda o actualiza un vehiculo
    Vehicle save(Vehicle vehicle);

    // Busca un vehiculo especifico segun su matricula
    Optional<Vehicle> findByPlate(String plate);

    // Nuevo método para verificar si el vehículo existe
    boolean existsByPlate(String plate);
}