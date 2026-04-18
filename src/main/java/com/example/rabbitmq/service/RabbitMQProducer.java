package com.example.rabbitmq.service;

import com.example.rabbitmq.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * RabbitMQ Producer Service
 * 
 * Responsible for sending messages to RabbitMQ over mTLS connection.
 */
@Service
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Send a message to the RabbitMQ tutorial queue
     * 
     * @param message The message content to send
     */
    public void sendMessage(String message) {
        String formattedMessage = String.format("[%s] %s", LocalDateTime.now().format(formatter), message);
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.TUTORIAL_EXCHANGE_NAME,
            RabbitMQConfig.TUTORIAL_ROUTING_KEY,
            formattedMessage
        );
        System.out.println("✅ Message sent successfully over mTLS");
    }
}
