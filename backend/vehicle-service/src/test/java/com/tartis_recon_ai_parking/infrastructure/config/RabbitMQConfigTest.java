package com.tartis_recon_ai_parking.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import static org.assertj.core.api.Assertions.assertThat;

class RabbitMQConfigTest {

    private final RabbitMQConfig config = new RabbitMQConfig();

    @Test
    void shouldDeclareTheSharedParkingExchange() {
        TopicExchange exchange = config.parkingEventsExchange();

        assertThat(exchange.getName()).isEqualTo("parking-events-exchange");
        assertThat(RabbitMQConfig.ROUTING_KEY_VEHICLE_CHANGED).isEqualTo("vehicle-changed-v1");
    }

    @Test
    void shouldUseJsonMessageConverter() {
        MessageConverter converter = config.jsonMessageConverter();

        assertThat(converter).isInstanceOf(Jackson2JsonMessageConverter.class);
    }
}
