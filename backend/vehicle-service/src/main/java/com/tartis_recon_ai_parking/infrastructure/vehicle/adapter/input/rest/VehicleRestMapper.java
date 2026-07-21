package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.tartis_recon_ai_parking.application.vehicle.dto.VehicleCreateDTO;
import com.tartis_recon_ai_parking.application.vehicle.dto.VehicleDTO;
import com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest.dto.request.VehicleRequest;
import com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest.dto.response.VehicleResponse;

// Traduce entre el contrato HTTP (Request/Response) y los DTO de aplicacion.
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface VehicleRestMapper {

    VehicleCreateDTO toCreateDTO(VehicleRequest request);

    VehicleResponse toResponse(VehicleDTO vehicle);

    Iterable<VehicleResponse> toResponseList(Iterable<VehicleDTO> vehicles);
}
