package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.output.persistence;

import com.tartis_recon_ai_parking.application.vehicle.port.output.VehiclePersistence;
import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import org.springframework.stereotype.Component;

import java.util.Optional;

// Adaptador de Persistencia (Driven Adapter) para la entidad Vehicle que implementa 
// el puerto de salida e inyecta tanto el repositorio como el mapper.
// Su función principal es orquestar la conversión y el guardado.
@Component
public class VehiclePersistenceAdapter implements VehiclePersistence {

    private final VehicleRepository repository;
    private final VehiclePersistenceMapper mapper;

    public VehiclePersistenceAdapter(VehicleRepository repository, VehiclePersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // Traduce la entidad, la guarda en la base de datos y devuelve el modelo de dominio actualizado.
    @Override
    public Vehicle save(Vehicle vehicle) {
        VehicleEntity entity = mapper.toEntity(vehicle);
        VehicleEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    // Busca en la base de datos por matrícula y mapea el resultado a dominio si existe.
    @Override
    public Optional<Vehicle> findByPlate(String plate) {

        return repository.findByPlate(plate).map(mapper::toDomain);
    }

}
