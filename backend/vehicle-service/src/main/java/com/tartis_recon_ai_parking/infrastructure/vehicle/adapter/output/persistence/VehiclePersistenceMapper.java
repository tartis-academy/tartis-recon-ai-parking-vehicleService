package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.output.persistence;

import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import org.mapstruct.Mapper;

// Mapper de infraestructura utilizado para traducir entre el modelo de Dominio y el modelo de Base de Datos.
@Mapper(componentModel = "spring")
public interface VehiclePersistenceMapper {

    // MapStruct mapeará automáticamente todos los campos que se llaman igual (plate, brand, model, etc.)
    VehicleEntity toEntity(Vehicle vehicle);

    Vehicle toDomain(VehicleEntity entity);
}