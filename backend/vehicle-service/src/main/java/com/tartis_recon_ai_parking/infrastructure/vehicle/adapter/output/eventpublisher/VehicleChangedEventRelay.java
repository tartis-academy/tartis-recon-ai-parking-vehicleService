package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.output.eventpublisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tartis_recon_ai_parking.application.vehicle.dto.VehicleChangedEvent;
import com.tartis_recon_ai_parking.application.vehicle.port.output.VehicleEventPublisher;

/**
 * Los use cases publican VehicleChangedEvent como evento de aplicacion
 * (in-process) dentro de su @Transactional; este listener lo reenvia al
 * VehicleEventPublisher real solo si la transaccion hace commit, evitando que
 * RabbitMQ reciba un evento de un cambio que finalmente no quedo persistido.
 */
@Component
public class VehicleChangedEventRelay {

    private static final Logger log = LoggerFactory.getLogger(VehicleChangedEventRelay.class);

    private final VehicleEventPublisher eventPublisher;

    public VehicleChangedEventRelay(VehicleEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVehicleChanged(VehicleChangedEvent event) {
        try {
            eventPublisher.publish(event);
        } catch (RuntimeException e) {
            log.error("No se pudo publicar el evento {} de cambio para el vehiculo {}",
                    event.eventId(), event.data().vehicleId(), e);
        }
    }
}
