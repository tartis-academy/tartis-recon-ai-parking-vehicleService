package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.output.eventpublisher;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.tartis_recon_ai_parking.application.vehicle.dto.VehicleChangedEvent;
import com.tartis_recon_ai_parking.application.vehicle.port.output.VehicleEventPublisher;
import com.tartis_recon_ai_parking.domain.vehicle.VehicleType;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class VehicleChangedEventRelayTest {

    private final VehicleEventPublisher eventPublisher = mock(VehicleEventPublisher.class);
    private final VehicleChangedEventRelay relay = new VehicleChangedEventRelay(eventPublisher);

    private VehicleChangedEvent anEvent() {
        return new VehicleChangedEvent(
                UUID.randomUUID(), "VehicleChangedEvent", "v1", Instant.now(),
                new VehicleChangedEvent.VehicleChangedData(UUID.randomUUID(), "1234BCD", VehicleType.CAR,
                        "Toyota", "Corolla", "Red", 4, false, true));
    }

    @Test
    void shouldForwardEventToPublisher() {
        VehicleChangedEvent event = anEvent();

        relay.onVehicleChanged(event);

        verify(eventPublisher).publish(event);
    }

    @Test
    void shouldNotPropagateFailuresOfTheBroker() {
        // El relay corre en AFTER_COMMIT: la transaccion ya termino y propagar
        // aqui no revierte nada, solo rompe el hilo del listener.
        VehicleChangedEvent event = anEvent();
        doThrow(new RuntimeException("rabbit caido")).when(eventPublisher).publish(event);

        assertThatCode(() -> relay.onVehicleChanged(event)).doesNotThrowAnyException();
    }
}
