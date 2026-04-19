package com.example.rabbitmq.service;

import com.example.rabbitmq.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RabbitMQ Producer Service
 * 
 * Responsible for sending messages to RabbitMQ over mTLS connection.
 */
@Service
public class RabbitMQProducer {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQProducer.class);
    private final RabbitTemplate rabbitTemplate;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String VHOST = "/tutorial";
    private String hostName;
    private String hostAddress;
    private final AtomicInteger messageCount = new AtomicInteger(0);

    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        
        // Initialize host information
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            this.hostName = inetAddress.getHostName();
            this.hostAddress = inetAddress.getHostAddress();
            logger.info("🚀 Producer initialized on host: {} ({}) for vhost: {}", 
                       hostName, hostAddress, VHOST);
        } catch (UnknownHostException e) {
            this.hostName = "UNKNOWN";
            this.hostAddress = "UNKNOWN";
            logger.warn("Unable to determine local host", e);
        }
    }

    /**
     * Send a message to the RabbitMQ tutorial queue
     * 
     * @param message The message content to send
     */
    public void sendMessage(String message) {
        int count = messageCount.incrementAndGet();
        String formattedMessage = String.format("[%s] %s", LocalDateTime.now().format(formatter), message);
        
        // Log sending information with vhost confirmation
        logger.info("📤 Sending message #{}", count);
        logger.info("   🏠 From Host: {} ({})", hostName, hostAddress);
        logger.info("   🏢 To Virtual Host (vhost): {}", VHOST);
        logger.info("   🔄 Exchange: {}", RabbitMQConfig.TUTORIAL_EXCHANGE_NAME);
        logger.info("   🔑 Routing Key: {}", RabbitMQConfig.TUTORIAL_ROUTING_KEY);
        logger.info("   📨 Target Queue: {}", RabbitMQConfig.TUTORIAL_QUEUE_NAME);
        logger.debug("   📄 Message Content: {}", formattedMessage);
        
        System.out.println("\n📤 Sending message #" + count);
        System.out.println("   🏠 From Host: " + hostName + " (" + hostAddress + ")");
        System.out.println("   🏢 To Virtual Host (vhost): " + VHOST);
        System.out.println("   🔄 Exchange: " + RabbitMQConfig.TUTORIAL_EXCHANGE_NAME);
        System.out.println("   🔑 Routing Key: " + RabbitMQConfig.TUTORIAL_ROUTING_KEY);
        System.out.println("   📨 Target Queue: " + RabbitMQConfig.TUTORIAL_QUEUE_NAME);
        System.out.println("   💬 Message: " + formattedMessage);
        
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.TUTORIAL_EXCHANGE_NAME,
            RabbitMQConfig.TUTORIAL_ROUTING_KEY,
            formattedMessage
        );
        
        logger.info("✅ Message #{} sent successfully over mTLS to vhost: {}", count, VHOST);
        System.out.println("✅ Message #{} sent successfully to vhost: " + VHOST + "\n");
    }
}
