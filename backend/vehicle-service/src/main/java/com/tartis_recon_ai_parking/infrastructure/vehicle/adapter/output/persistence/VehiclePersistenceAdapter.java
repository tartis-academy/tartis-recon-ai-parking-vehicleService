package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.output.persistence;

import com.tartis_recon_ai_parking.application.vehicle.port.output.VehiclePersistence;
import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.domain.vehicle.exception.ExistingVehicleException;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleConcurrentModificationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
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
        VehicleEntity entity = vehiclePersistenceMapper.toEntity(vehicle);
        try {
            // saveAndFlush obliga a ejecutar el INSERT/UPDATE ahora mismo, dentro
            // de este try: con un save() normal el fallo saldria al hacer commit,
            // fuera del catch, y no se traduciria.
            VehicleEntity savedEntity = vehicleRepository.saveAndFlush(entity);
            return vehiclePersistenceMapper.toDomain(savedEntity);
        } catch (OptimisticLockingFailureException e) {
            // @Version detecto que otra transaccion modifico la fila entre la
            // lectura y el guardado. Va antes que DataIntegrityViolationException:
            // son ramas hermanas de DataAccessException, pero el orden importa si
            // alguien generaliza el segundo catch en el futuro.
            throw new VehicleConcurrentModificationException(vehicle.getPlate(), e);
        } catch (DataIntegrityViolationException e) {
            // Choque de dos hilos guardando la misma matricula (constraint
            // vehicles_plate_key). No se filtra por nombre de constraint: la unica
            // unicidad de esta tabla es la de plate, asi que traducir sin condicion
            // es correcto.
            throw new ExistingVehicleException(vehicle.getPlate(), e);
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
