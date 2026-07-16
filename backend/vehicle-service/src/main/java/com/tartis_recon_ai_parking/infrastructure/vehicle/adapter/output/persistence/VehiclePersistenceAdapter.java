package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.output.persistence;

import com.tartis_recon_ai_parking.application.vehicle.port.output.VehiclePersistence;
import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.List;

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
        VehicleEntity entity = vehiclePersistenceMapper.toEntity(vehicle);
        VehicleEntity savedEntity = vehicleRepository.save(entity);
        return vehiclePersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Vehicle> findByPlate(String plate) {
        return vehicleRepository.findByPlate(plate)
                .map(vehiclePersistenceMapper::toDomain);
    }

    @Override
    public Optional<Vehicle> findById(Long id) {
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