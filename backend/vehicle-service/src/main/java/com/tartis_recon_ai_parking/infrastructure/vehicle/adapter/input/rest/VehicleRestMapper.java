package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest.dto.response.VehicleResponse;

// unmappedTargetPolicy = ERROR: un campo de la response sin origen rompe el build
// en vez de devolverse como null en el JSON sin avisar.
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface VehicleRestMapper {
    VehicleResponse toResponse(Vehicle vehicle);

    Iterable<VehicleResponse> toResponseList(Iterable<Vehicle> vehicles);
}
