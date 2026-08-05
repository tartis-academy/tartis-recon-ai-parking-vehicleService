package com.tartis_recon_ai_parking.application.vehicle.usecase;

import com.tartis_recon_ai_parking.application.vehicle.dto.VehicleChangedEvent;
import com.tartis_recon_ai_parking.application.vehicle.dto.VehicleCreateDTO;
import com.tartis_recon_ai_parking.application.vehicle.port.output.VehiclePersistence;
import com.tartis_recon_ai_parking.domain.vehicle.Vehicle;
import com.tartis_recon_ai_parking.domain.vehicle.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleChangedEventEmissionTest {

    @Mock
    private VehiclePersistence vehiclePersistence;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private final ArgumentCaptor<VehicleChangedEvent> eventCaptor =
            ArgumentCaptor.forClass(VehicleChangedEvent.class);

    @Test
    @DisplayName("Crear un vehiculo emite VehicleChangedEvent con el contrato v1")
    void shouldPublishEventOnCreate() {
        when(vehiclePersistence.existsByPlate("1234BCD")).thenReturn(false);
        when(vehiclePersistence.save(any(Vehicle.class))).thenAnswer(i -> i.getArgument(0));

        new CreateVehicleUseCase(vehiclePersistence, applicationEventPublisher)
                .execute(new VehicleCreateDTO("CAR", "1234BCD", "Toyota", "Corolla", "Red", 4, false));

        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        VehicleChangedEvent event = eventCaptor.getValue();
        assertThat(event.type()).isEqualTo("VehicleChangedEvent");
        assertThat(event.version()).isEqualTo("v1");
        assertThat(event.eventId()).isNotNull();
        assertThat(event.occurredAt()).isNotNull();
        assertThat(event.data().plate()).isEqualTo("1234BCD");
        assertThat(event.data().vehicleType()).isEqualTo(VehicleType.CAR);
        assertThat(event.data().brand()).isEqualTo("Toyota");
        assertThat(event.data().model()).isEqualTo("Corolla");
        assertThat(event.data().color()).isEqualTo("Red");
        assertThat(event.data().numDoors()).isEqualTo(4);
        assertThat(event.data().hasSidecar()).isFalse();
        assertThat(event.data().active()).isTrue();
    }

    @Test
    @DisplayName("Actualizar un vehiculo emite VehicleChangedEvent con los datos ya guardados")
    void shouldPublishEventOnUpdate() {
        UUID id = UUID.randomUUID();
        Vehicle existing = Vehicle.reconstruct(id, 1L, VehicleType.CAR, "1234BCD", "Toyota", "Corolla", "Red", 4, false, true);
        when(vehiclePersistence.findById(id)).thenReturn(Optional.of(existing));
        when(vehiclePersistence.findByPlate("1234BCD")).thenReturn(Optional.of(existing));
        when(vehiclePersistence.save(any(Vehicle.class))).thenAnswer(i -> i.getArgument(0));

        new UpdateVehicleUseCase(vehiclePersistence, applicationEventPublisher)
                .execute(id, new VehicleCreateDTO("CAR", "1234BCD", "Toyota", "Corolla", "Blue", 4, false));

        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().data().vehicleId()).isEqualTo(id);
        assertThat(eventCaptor.getValue().data().color()).isEqualTo("Blue");
    }

    @Test
    @DisplayName("Activar un vehiculo inactivo emite VehicleChangedEvent con active=true")
    void shouldPublishEventOnActivate() {
        UUID id = UUID.randomUUID();
        Vehicle inactive = Vehicle.reconstruct(id, 1L, VehicleType.CAR, "1234BCD", "Toyota", "Corolla", "Red", 4, false, false);
        when(vehiclePersistence.findById(id)).thenReturn(Optional.of(inactive));
        when(vehiclePersistence.save(any(Vehicle.class))).thenAnswer(i -> i.getArgument(0));

        new ActivateVehicleUseCase(vehiclePersistence, applicationEventPublisher).activate(id);

        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().data().active()).isTrue();
    }

    @Test
    @DisplayName("Activar un vehiculo ya activo no emite evento")
    void shouldNotPublishEventWhenActivatingAnAlreadyActiveVehicle() {
        UUID id = UUID.randomUUID();
        Vehicle active = Vehicle.reconstruct(id, 1L, VehicleType.CAR, "1234BCD", "Toyota", "Corolla", "Red", 4, false, true);
        when(vehiclePersistence.findById(id)).thenReturn(Optional.of(active));
        when(vehiclePersistence.save(any(Vehicle.class))).thenAnswer(i -> i.getArgument(0));

        new ActivateVehicleUseCase(vehiclePersistence, applicationEventPublisher).activate(id);

        verify(applicationEventPublisher, never()).publishEvent(any(VehicleChangedEvent.class));
    }

    @Test
    @DisplayName("Desactivar un vehiculo activo emite VehicleChangedEvent con active=false")
    void shouldPublishEventOnDeactivate() {
        UUID id = UUID.randomUUID();
        Vehicle active = Vehicle.reconstruct(id, 1L, VehicleType.CAR, "1234BCD", "Toyota", "Corolla", "Red", 4, false, true);
        when(vehiclePersistence.findById(id)).thenReturn(Optional.of(active));
        when(vehiclePersistence.save(any(Vehicle.class))).thenAnswer(i -> i.getArgument(0));

        new DeleteVehicleUseCase(vehiclePersistence, applicationEventPublisher).deactivate(id);

        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().data().active()).isFalse();
    }

    @Test
    @DisplayName("Desactivar un vehiculo ya inactivo no emite evento")
    void shouldNotPublishEventWhenDeactivatingAnAlreadyInactiveVehicle() {
        UUID id = UUID.randomUUID();
        Vehicle inactive = Vehicle.reconstruct(id, 1L, VehicleType.CAR, "1234BCD", "Toyota", "Corolla", "Red", 4, false, false);
        when(vehiclePersistence.findById(id)).thenReturn(Optional.of(inactive));
        when(vehiclePersistence.save(any(Vehicle.class))).thenAnswer(i -> i.getArgument(0));

        new DeleteVehicleUseCase(vehiclePersistence, applicationEventPublisher).deactivate(id);

        verify(applicationEventPublisher, never()).publishEvent(any(VehicleChangedEvent.class));
    }
}
