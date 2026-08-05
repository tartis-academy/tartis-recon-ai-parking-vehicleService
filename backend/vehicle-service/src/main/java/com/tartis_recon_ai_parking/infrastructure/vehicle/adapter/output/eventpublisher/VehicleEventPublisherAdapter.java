package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.output.eventpublisher;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.tartis_recon_ai_parking.application.vehicle.dto.VehicleChangedEvent;
import com.tartis_recon_ai_parking.application.vehicle.port.output.VehicleEventPublisher;
import com.tartis_recon_ai_parking.infrastructure.config.RabbitMQConfig;

@Component
public class VehicleEventPublisherAdapter implements VehicleEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public VehicleEventPublisherAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(VehicleChangedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_VEHICLE_CHANGED,
                event);
    }
}
