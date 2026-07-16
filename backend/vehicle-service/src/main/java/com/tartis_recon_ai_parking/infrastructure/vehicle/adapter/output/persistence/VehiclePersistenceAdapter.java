package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.output.persistence;

import com.tartis_recon_ai_parking.application.vehicle.port.output.VehiclePersistence;
import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class VehiclePersistenceAdapter implements VehiclePersistence {

    private final VehicleRepository repository;
    private final VehiclePersistenceMapper mapper;

    public VehiclePersistenceAdapter(VehicleRepository repository, VehiclePersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        VehicleEntity entity = mapper.toEntity(vehicle);
        VehicleEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Vehicle> findByPlate(String plate) {

        return repository.findByPlate(plate).map(mapper::toDomain);
    }

}
