package com.example.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration with queue, exchange, and binding definitions.
 * 
 * This configuration demonstrates:
 * - Queue declaration
 * - Direct Exchange declaration
 * - Queue binding to exchange with routing key
 * 
 * The mTLS/SSL settings are configured via application.yml and automatically
 * picked up by Spring Boot's auto-configuration.
 */
@Configuration
public class RabbitMQConfig {

    // Queue names
    public static final String TUTORIAL_QUEUE_NAME = "tutorial.queue";
    public static final String TUTORIAL_EXCHANGE_NAME = "tutorial.exchange";
    public static final String TUTORIAL_ROUTING_KEY = "tutorial.key";

    /**
     * Declare the tutorial queue.
     * durable = true: Queue persists across server restarts
     */
    @Bean
    public Queue tutorialQueue() {
        return new Queue(TUTORIAL_QUEUE_NAME, true);
    }

    /**
     * Declare a direct exchange for routing messages.
     * durable = true: Exchange persists across server restarts
     */
    @Bean
    public DirectExchange tutorialExchange() {
        return new DirectExchange(TUTORIAL_EXCHANGE_NAME, true, false);
    }

    /**
     * Bind the queue to the exchange with the routing key.
     * This tells RabbitMQ to route messages from the exchange to this queue
     * when the routing key matches.
     */
    @Bean
    public Binding tutorialBinding(Queue tutorialQueue, DirectExchange tutorialExchange) {
        return BindingBuilder.bind(tutorialQueue)
                .to(tutorialExchange)
                .with(TUTORIAL_ROUTING_KEY);
    }

}
