package com.tartis_recon_ai_parking.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import com.tartis_recon_ai_parking.application.vehicle.port.output.VehiclePersistence;
import com.tartis_recon_ai_parking.application.vehicle.usecase.ActivateVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.CreateVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.DeleteVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.GetVehicleUseCase;
import com.tartis_recon_ai_parking.application.vehicle.usecase.UpdateVehicleUseCase;

class BeanConfigurationTest {

    private final VehiclePersistence mockPersistence = Mockito.mock(VehiclePersistence.class);
    private final ApplicationEventPublisher mockEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
    private final BeanConfiguration config = new BeanConfiguration();

    @Test
    @DisplayName("Debe instanciar los cinco beans de casos de uso")
    void shouldCreateAllUseCaseBeans() {
        assertThat(config.createVehicleUseCase(mockPersistence, mockEventPublisher)).isInstanceOf(CreateVehicleUseCase.class);
        assertThat(config.deleteVehicleUseCase(mockPersistence, mockEventPublisher)).isInstanceOf(DeleteVehicleUseCase.class);
        assertThat(config.activateVehicleUseCase(mockPersistence, mockEventPublisher)).isInstanceOf(ActivateVehicleUseCase.class);
        assertThat(config.updateVehicleUseCase(mockPersistence, mockEventPublisher)).isInstanceOf(UpdateVehicleUseCase.class);
        assertThat(config.getVehicleUseCase(mockPersistence)).isInstanceOf(GetVehicleUseCase.class);
    }
}
