package com.tartis_recon_ai_parking.application.vehicle.usecase;

import com.tartis_recon_ai_parking.application.vehicle.dto.VehicleDTO;
import com.tartis_recon_ai_parking.application.vehicle.port.output.VehiclePersistence;
import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.domain.vehicle.VehicleType;
import com.tartis_recon_ai_parking.domain.vehicle.exception.VehicleNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivateVehicleUseCaseTest {

    @Mock
    private VehiclePersistence vehiclePersistence;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private ActivateVehicleUseCase activateVehicleUseCase;

    @Test
    @DisplayName("Debe activar el vehiculo (active=true) y guardar si el vehiculo existe inactivo")
    void shouldActivateVehicleSuccessfully() throws VehicleNotFoundException {
        UUID id = UUID.randomUUID();
        Vehicle vehicle = Vehicle.reconstruct(id, 1L, VehicleType.CAR, "1234BCD", "Toyota", "Corolla", "Red", 4, false, false);

        when(vehiclePersistence.findById(id)).thenReturn(Optional.of(vehicle));
        when(vehiclePersistence.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VehicleDTO result = activateVehicleUseCase.activate(id);

        ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehiclePersistence, times(1)).save(vehicleCaptor.capture());

        Vehicle savedVehicle = vehicleCaptor.getValue();
        assertThat(savedVehicle.isActive()).isTrue();
        assertThat(result.active()).isTrue();
    }

    @Test
    @DisplayName("Debe mantener active=true de forma idempotente al activar un vehiculo ya activo")
    void shouldMaintainActiveTrueWhenVehicleAlreadyActive() throws VehicleNotFoundException {
        UUID id = UUID.randomUUID();
        Vehicle vehicle = Vehicle.reconstruct(id, 1L, VehicleType.CAR, "1234BCD", "Toyota", "Corolla", "Red", 4, false, true);

        when(vehiclePersistence.findById(id)).thenReturn(Optional.of(vehicle));
        when(vehiclePersistence.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VehicleDTO result = activateVehicleUseCase.activate(id);

        ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehiclePersistence, times(1)).save(vehicleCaptor.capture());

        Vehicle savedVehicle = vehicleCaptor.getValue();
        assertThat(savedVehicle.isActive()).isTrue();
        assertThat(result.active()).isTrue();
    }

    @Test
    @DisplayName("Debe lanzar VehicleNotFoundException al activar si el vehiculo no existe")
    void shouldThrowExceptionWhenActivatingNonExistentVehicle() {
        UUID id = UUID.randomUUID();

        when(vehiclePersistence.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activateVehicleUseCase.activate(id))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessageContaining("Vehicle with 'ID = " + id + "' couldn't be found.");

        verify(vehiclePersistence, never()).save(any(Vehicle.class));
    }
}
