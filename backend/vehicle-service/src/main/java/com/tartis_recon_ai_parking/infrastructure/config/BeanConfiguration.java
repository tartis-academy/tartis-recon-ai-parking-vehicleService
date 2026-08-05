
package com.tartis_recon_ai_parking.infrastructure.config;

import com.tartis_recon_ai_parking.application.vehicle.port.output.VehiclePersistence;
import com.tartis_recon_ai_parking.application.vehicle.usecase.ActivateVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.CreateVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.DeleteVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.GetVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.UpdateVehicleUseCase;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    CreateVehicleUseCase createVehicleUseCase(VehiclePersistence vehiclePersistence,
            ApplicationEventPublisher applicationEventPublisher) {
        return new CreateVehicleUseCase(vehiclePersistence, applicationEventPublisher);
    }
    @Bean
    DeleteVehicleUseCase deleteVehicleUseCase(VehiclePersistence vehiclePersistence,
            ApplicationEventPublisher applicationEventPublisher) {
        return new DeleteVehicleUseCase(vehiclePersistence, applicationEventPublisher);
    }
    @Bean
    ActivateVehicleUseCase activateVehicleUseCase(VehiclePersistence vehiclePersistence,
            ApplicationEventPublisher applicationEventPublisher) {
        return new ActivateVehicleUseCase(vehiclePersistence, applicationEventPublisher);
    }
    @Bean
    UpdateVehicleUseCase updateVehicleUseCase(VehiclePersistence vehiclePersistence,
            ApplicationEventPublisher applicationEventPublisher) {
        return new UpdateVehicleUseCase(vehiclePersistence, applicationEventPublisher);
    }
    @Bean
    GetVehicleUseCase getVehicleUseCase(VehiclePersistence vehiclePersistence) {
        return new GetVehicleUseCase(vehiclePersistence);
    }
}