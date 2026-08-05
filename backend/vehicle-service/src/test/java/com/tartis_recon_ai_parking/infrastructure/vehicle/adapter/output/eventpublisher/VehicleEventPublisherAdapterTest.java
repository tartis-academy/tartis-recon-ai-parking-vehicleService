package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.output.eventpublisher;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.tartis_recon_ai_parking.application.vehicle.dto.VehicleChangedEvent;
import com.tartis_recon_ai_parking.domain.vehicle.VehicleType;
import com.tartis_recon_ai_parking.infrastructure.config.RabbitMQConfig;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class VehicleEventPublisherAdapterTest {

    private final RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    private final VehicleEventPublisherAdapter adapter = new VehicleEventPublisherAdapter(rabbitTemplate);

    @Test
    void shouldPublishToSharedExchangeWithVehicleRoutingKey() {
        VehicleChangedEvent event = new VehicleChangedEvent(
                UUID.randomUUID(), "VehicleChangedEvent", "v1", Instant.now(),
                new VehicleChangedEvent.VehicleChangedData(UUID.randomUUID(), "1234BCD", VehicleType.CAR,
                        "Toyota", "Corolla", "Red", 4, false, true));

        adapter.publish(event);

        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_VEHICLE_CHANGED,
                event);
    }
}
