package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.output.persistence;

import com.tartis_recon_ai_parking.application.vehicle.port.output.VehiclePersistence;
import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.domain.vehicle.exception.ExistingVehicleException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Component
public class VehiclePersistenceAdapter implements VehiclePersistence {

    private final VehicleRepository vehicleRepository;
    private final VehiclePersistenceMapper vehiclePersistenceMapper;

    public VehiclePersistenceAdapter(VehicleRepository vehicleRepository, VehiclePersistenceMapper vehiclePersistenceMapper) {
        this.vehicleRepository = vehicleRepository;
        this.vehiclePersistenceMapper = vehiclePersistenceMapper;
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        try {
        VehicleEntity entity = vehiclePersistenceMapper.toEntity(vehicle);
        
        // saveAndFlush obliga a ejecutar el INSERT/UPDATE ahora mismo
        VehicleEntity savedEntity = vehicleRepository.saveAndFlush(entity);
        
        return vehiclePersistenceMapper.toDomain(savedEntity);
    } catch (DataIntegrityViolationException e) {
        // Capturamos el choque de dos hilos guardando la misma matrícula
        throw new ExistingVehicleException(vehicle.getPlate());
    }
    }

    @Override
    public Optional<Vehicle> findByPlate(String plate) {
        return vehicleRepository.findByPlate(plate)
                .map(vehiclePersistenceMapper::toDomain);
    }

    @Override
    public Optional<Vehicle> findById(UUID id) {
        return vehicleRepository.findById(id)
                .map(entity -> vehiclePersistenceMapper.toDomain(entity));
    }

    // Implementación necesaria para satisfacer la interfaz
    @Override
    public boolean existsByPlate(String plate) {
        return vehicleRepository.existsByPlate(plate);
    }

    @Override
    public List<Vehicle> findAll() {
        return vehicleRepository.findAll() //traemos la lista desd BD
                .stream() //convertimos lista en un flujo (mapear uno a uno)
                .map(vehiclePersistenceMapper::toDomain) //convertimos cada Entity a domain
                .toList();  //agrupamos d nuevo en lista
    }
}