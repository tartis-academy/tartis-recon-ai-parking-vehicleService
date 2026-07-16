package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest;

import org.mapstruct.Mapper;

import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest.dto.response.VehicleResponse;

@Mapper(componentModel = "spring")
public interface VehicleRestMapper {
    VehicleResponse toResponse(Vehicle vehicle);

    Iterable<VehicleResponse> toResponseList(Iterable<Vehicle> vehicles);
}
