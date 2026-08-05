package com.tartis_recon_ai_parking.infrastructure.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "parking-events-exchange";
    public static final String ROUTING_KEY_VEHICLE_CHANGED = "vehicle-changed-v1";

    // vehicle-service solo publica: la cola y su dead-lettering los declara
    // stay-service, que es quien conoce sus propios argumentos.
    @Bean
    public TopicExchange parkingEventsExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
