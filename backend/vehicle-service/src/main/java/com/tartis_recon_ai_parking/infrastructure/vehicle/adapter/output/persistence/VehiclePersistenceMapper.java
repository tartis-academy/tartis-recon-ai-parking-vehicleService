package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.output.persistence;

import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// Mapper de infraestructura utilizado para traducir entre el modelo de Dominio y el modelo de Base de Datos.
@Mapper(componentModel = "spring")
public interface VehiclePersistenceMapper {

    // Convierte un objeto del modelo de dominio (Vehicle) a una entidad tecnológica (VehicleEntity).
    @Mapping(source = "id", target = "uniqueId")
    VehicleEntity toEntity(Vehicle vehicle);

    // Convierte una entidad tecnológica (VehicleEntity) recuperada de la base de datos de vuelta al modelo de dominio puro (Vehicle).
    @Mapping(source = "uniqyeId", target = "id")
    Vehicle toDomain(VehicleEntity entity);

}