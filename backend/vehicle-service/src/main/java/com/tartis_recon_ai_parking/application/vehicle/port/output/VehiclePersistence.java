package com.tartis_recon_ai_parking.application.vehicle.port.output;

import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import java.util.Optional;

public interface VehiclePersistence {

    Vehicle save(Vehicle vechicle);
    Optional<Vehicle> findByPlate(String plate);
}
