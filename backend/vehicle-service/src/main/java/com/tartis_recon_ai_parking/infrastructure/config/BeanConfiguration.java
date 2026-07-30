
package com.tartis_recon_ai_parking.infrastructure.config;

import com.tartis_recon_ai_parking.application.vehicle.port.output.VehiclePersistence;
import com.tartis_recon_ai_parking.application.vehicle.usecase.ActivateVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.CreateVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.DeleteVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.GetVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.UpdateVehicleUseCase;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    CreateVehicleUseCase createVehicleUseCase(VehiclePersistence vehiclePersistence) {
        return new CreateVehicleUseCase(vehiclePersistence);
    }
    @Bean
    DeleteVehicleUseCase deleteVehicleUseCase(VehiclePersistence vehiclePersistence) {
        return new DeleteVehicleUseCase(vehiclePersistence);
    }
    @Bean
    ActivateVehicleUseCase activateVehicleUseCase(VehiclePersistence vehiclePersistence) {
        return new ActivateVehicleUseCase(vehiclePersistence);
    }
    @Bean
    UpdateVehicleUseCase updateVehicleUseCase(VehiclePersistence vehiclePersistence) {
        return new UpdateVehicleUseCase(vehiclePersistence);
    }
    @Bean
    GetVehicleUseCase getVehicleUseCase(VehiclePersistence vehiclePersistence) {
        return new GetVehicleUseCase(vehiclePersistence);
    }
}