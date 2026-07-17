package com.tartis_recon_ai_parking.application.vehicle.usecase;

import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.domain.vehicle.exception.InvalidVehicleException;
import com.tartis_recon_ai_parking.application.vehicle.dto.VehicleCreateDTO;
import com.tartis_recon_ai_parking.application.vehicle.dto.VehicleDTO;
import com.tartis_recon_ai_parking.application.vehicle.factory.VehicleDTOFactory;
import com.tartis_recon_ai_parking.application.vehicle.port.output.VehiclePersistence;

public class CreateVehicleUseCase {

    private final VehiclePersistence vehiclePersistence;

    public CreateVehicleUseCase(VehiclePersistence vehiclePersistence) {
        this.vehiclePersistence = vehiclePersistence;
    }

    public VehicleDTO execute(VehicleCreateDTO createDTO) throws InvalidVehicleException {
        // Antes que existsByPlate: si los datos son invalidos debe ganar ese
        // error, no el de matricula duplicada.
        Vehicle vehicle = VehicleDTOFactory.toDomain(createDTO);

        if (vehiclePersistence.existsByPlate(vehicle.getPlate())) {
            throw new InvalidVehicleException("Ya existe un vehículo con la matrícula: " + vehicle.getPlate());
        }
        return VehicleDTOFactory.toDTO(vehiclePersistence.save(vehicle));
    }
}
