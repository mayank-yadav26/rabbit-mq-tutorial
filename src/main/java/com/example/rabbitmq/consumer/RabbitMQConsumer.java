package com.example.rabbitmq.consumer;

import com.example.rabbitmq.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RabbitMQ Consumer Service
 * 
 * Listens for messages from the tutorial queue and processes them.
 */
@Service
public class RabbitMQConsumer {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConsumer.class);
    private final AtomicInteger messageCount = new AtomicInteger(0);
    private String hostName;
    private String hostAddress;
    private static final String VHOST = "/tutorial";

    /**
     * Constructor - Initialize hostname and host address
     */
    public RabbitMQConsumer() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            this.hostName = inetAddress.getHostName();
            this.hostAddress = inetAddress.getHostAddress();
            logger.info("🚀 Consumer initialized on host: {} ({}) for vhost: {}", 
                       hostName, hostAddress, VHOST);
        } catch (UnknownHostException e) {
            this.hostName = "UNKNOWN";
            this.hostAddress = "UNKNOWN";
            logger.warn("Unable to determine local host", e);
        }
    }

    /**
     * Listen for messages from the tutorial queue
     * 
     * @param message The message object containing content and metadata
     * @param messageBody The message body content
     */
    @RabbitListener(queues = RabbitMQConfig.TUTORIAL_QUEUE_NAME)
    public void consumeMessage(Message message, String messageBody) {
        int count = messageCount.incrementAndGet();
        
        // Extract message metadata
        String exchange = new String(message.getMessageProperties().getReceivedExchange() != null 
                ? message.getMessageProperties().getReceivedExchange().getBytes() 
                : "N/A".getBytes());
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        String queueName = RabbitMQConfig.TUTORIAL_QUEUE_NAME;
        
        // Log with complete routing information
        logger.info("📥 Message #{} received", count);
        logger.info("   🏠 Host: {} ({})", hostName, hostAddress);
        logger.info("   🏢 Virtual Host (vhost): {}", VHOST);
        logger.info("   📨 Queue: {}", queueName);
        logger.info("   🔄 Exchange: {}", exchange);
        logger.info("   🔑 Routing Key: {}", routingKey);
        logger.debug("   📄 Message Content: {}", messageBody);
        
        System.out.println("📥 Received message #" + count + ": " + messageBody);
        System.out.println("   🏠 Host: " + hostName + " (" + hostAddress + ")");
        System.out.println("   🏢 Virtual Host (vhost): " + VHOST);
        System.out.println("   📨 Queue: " + queueName);
        System.out.println("   🔄 Exchange: " + exchange);
        System.out.println("   🔑 Routing Key: " + routingKey);
        
        // Process the message
        processMessage(messageBody);
        
        logger.info("✅ Message #{} processed successfully", count);
        System.out.println("✅ Message processed successfully");
    }

    /**
     * Process the received message
     * 
     * @param message The message to process
     */
    private void processMessage(String message) {
        logger.info("⚙️  Processing message on host: {} for vhost: {} - Content: {}", 
                   hostName, VHOST, message);
        
        // Add your processing logic here
        // For now, just a simple log
        System.out.println("   ⚙️  Processing: " + message);
    }

    /**
     * Get the total number of messages processed
     */
    public int getMessageCount() {
        return messageCount.get();
    }
}
