package com.example.rabbitmq.controller;

import com.example.rabbitmq.service.RabbitMQProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for sending messages to RabbitMQ
 */
@RestController
@RequestMapping("/api")
public class ProducerController {

    private final RabbitMQProducer rabbitMQProducer;

    public ProducerController(RabbitMQProducer rabbitMQProducer) {
        this.rabbitMQProducer = rabbitMQProducer;
    }

    /**
     * Send a message to RabbitMQ
     * 
     * @param message The message to send
     * @return Response with status
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendMessage(@RequestParam String message) {
        try {
            rabbitMQProducer.sendMessage(message);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Message sent successfully over mTLS");
            response.put("status", "success");
            response.put("content", message);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to send message");
            error.put("status", "error");
            error.put("error", e.getMessage());
            
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "RabbitMQ mTLS Tutorial is running");
        return ResponseEntity.ok(response);
    }
}
