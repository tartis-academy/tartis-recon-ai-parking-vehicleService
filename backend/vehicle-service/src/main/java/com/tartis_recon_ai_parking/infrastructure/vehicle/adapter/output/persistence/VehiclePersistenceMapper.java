package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.output.persistence;

import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;
import org.mapstruct.ReportingPolicy;

// Mapper de infraestructura utilizado para traducir entre el modelo de Dominio y el modelo de Base de Datos.
// unmappedTargetPolicy = ERROR: si un campo del destino no encuentra origen, la compilacion FALLA.
// Por defecto MapStruct solo emite un warning y genera el mapper sin ese campo, que es como
// 'hasSidecar' se dejo de persistir sin que nadie se enterara.
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface VehiclePersistenceMapper {

    // MapStruct mapeará automáticamente todos los campos que se llaman igual (plate, brand, model, etc.)
    VehicleEntity toEntity(Vehicle vehicle);

    Vehicle toDomain(VehicleEntity entity);

    @ObjectFactory
    default Vehicle reconstruct(VehicleEntity entity) {
        if (entity == null) {
            return null;
        }
        return Vehicle.reconstruct(
            entity.getUniqueId(),
            entity.getType(),
            entity.getPlate(),
            entity.getBrand(),
            entity.getModel(),
            entity.getColor(),
            entity.getNumDoors(),
            entity.getHasSidecar(),
            entity.getActive()
        );
    }
    
}