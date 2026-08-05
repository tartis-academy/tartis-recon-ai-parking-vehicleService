package com.tartis_recon_ai_parking.application.vehicle.port.output;

import com.tartis_recon_ai_parking.application.vehicle.dto.VehicleChangedEvent;

public interface VehicleEventPublisher {

    /**
     * Publica el evento VehicleChangedEvent hacia RabbitMQ. La llamada es
     * sincrona: el invocador (VehicleChangedEventRelay, en AFTER_COMMIT)
     * espera a que RabbitTemplate.convertAndSend retorne.
     */
    void publish(VehicleChangedEvent event);
}
