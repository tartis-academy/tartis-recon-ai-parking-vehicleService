package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.output.persistence;

import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VehiclePersistenceMapper {

    @Mapping(source = "id", target = "uniqueId")
    VehicleEntity toEntity(Vehicle vehicle);

    @Mapping(source = "uniqyeId", target = "id")
    Vehicle toDomain(VehicleEntity entity);

}