package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio Spring Data para la persistencia de entidades VehicleEntity.
 *
 * Es la interfaz estandar de Spring Data para manejar las operaciones en la base de datos.
 * Al extender de JpaRepository, delega a Spring la implementación en tiempo de ejecucion 
 * de las operaciones CRUD sobre la tabla de vehiculos.
 */
public interface VehicleRepository extends JpaRepository<VehicleEntity, Long> {

    // Busca una entidad de vehiculo utilizando la matricula
    Optional<VehicleEntity> findByPlate(String plate);

}
