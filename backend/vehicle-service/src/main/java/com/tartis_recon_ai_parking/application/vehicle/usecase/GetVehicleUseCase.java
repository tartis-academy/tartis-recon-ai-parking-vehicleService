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

    public VehicleDTO getByPlate(String plate) {
        return VehicleDTOFactory.toDTO(persistence.findByPlate(plate)
                .orElseThrow(() -> new VehicleNotFoundException("Plate", plate)));
    }

    public VehicleDTO getById(UUID id) {
        return VehicleDTOFactory.toDTO(persistence.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException("ID", id)));
    }

    public List<VehicleDTO> execute() {
        return VehicleDTOFactory.toDTOList(persistence.findAll());
    }
}
