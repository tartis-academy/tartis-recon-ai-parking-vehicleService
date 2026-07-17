package com.tartis_recon_ai_parking.application.vehicle.usecase;

import java.util.List;
import java.util.UUID;

import com.tartis_recon_ai_parking.application.vehicle.dto.VehicleDTO;
import com.tartis_recon_ai_parking.application.vehicle.factory.VehicleDTOFactory;
import com.tartis_recon_ai_parking.application.vehicle.port.output.VehiclePersistence;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleNotFoundException;

public class GetVehicleUseCase {
    private final VehiclePersistence persistence;

    public GetVehicleUseCase(VehiclePersistence persistence) {
        this.persistence = persistence;
    }

    public VehicleDTO getByPlate(String plate) throws VehicleNotFoundException {
        return VehicleDTOFactory.toDTO(persistence.findByPlate(plate)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle with plate " + plate + " not found")));
    }

    public VehicleDTO getById(UUID id) throws VehicleNotFoundException {
        return VehicleDTOFactory.toDTO(persistence.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle with ID " + id + " not found")));
    }

    public List<VehicleDTO> execute() {
        return VehicleDTOFactory.toDTOList(persistence.findAll());
    }
}
