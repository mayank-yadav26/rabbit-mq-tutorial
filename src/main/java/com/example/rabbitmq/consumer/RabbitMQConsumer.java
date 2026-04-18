package com.example.rabbitmq.consumer;

import com.example.rabbitmq.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RabbitMQ Consumer Service
 * 
 * Listens for messages from the tutorial queue and processes them.
 */
@Service
public class RabbitMQConsumer {

    private final AtomicInteger messageCount = new AtomicInteger(0);

    /**
     * Listen for messages from the tutorial queue
     * 
     * @param message The message received from RabbitMQ
     */
    @RabbitListener(queues = RabbitMQConfig.TUTORIAL_QUEUE_NAME)
    public void consumeMessage(String message) {
        int count = messageCount.incrementAndGet();
        System.out.println("📥 Received message #" + count + ": " + message);
        
        // Process the message
        processMessage(message);
        
        System.out.println("✅ Message processed successfully");
    }

    /**
     * Process the received message
     * 
     * @param message The message to process
     */
    private void processMessage(String message) {
        // Add your processing logic here
        // For now, just a simple log
        System.out.println("   Processing: " + message);
    }

    /**
     * Get the total number of messages processed
     */
    public int getMessageCount() {
        return messageCount.get();
    }
}
