package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional; // ¡Importante importar esto!

@Repository
public interface VehicleRepository extends JpaRepository<VehicleEntity, Long> {
    
    // Método para comprobar existencia
    boolean existsByPlate(String plate);
    
    // Método para buscar por placa
    Optional<VehicleEntity> findByPlate(String plate);
}