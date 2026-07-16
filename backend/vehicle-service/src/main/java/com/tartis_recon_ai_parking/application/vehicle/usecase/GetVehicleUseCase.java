package com.tartis_recon_ai_parking.application.vehicle.usecase;

import com.tartis_recon_ai_parking.application.vehicle.port.output.VehiclePersistence;
import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import java.util.List;

public class GetVehicleUseCase {

private final VehiclePersistence vehiclePersistence;

    public GetVehicleUseCase(VehiclePersistence vehiclePersistence) {
        this.vehiclePersistence = vehiclePersistence;
    }

    public List<Vehicle> execute() {
        return vehiclePersistence.findAll();
    }
}
