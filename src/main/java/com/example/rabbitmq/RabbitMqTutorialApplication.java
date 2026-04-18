package com.example.rabbitmq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot application for RabbitMQ with mTLS authentication.
 * 
 * This application demonstrates secure RabbitMQ communication using
 * mutual TLS (mTLS) authentication with client certificates.
 * 
 * Start with:
 * - docker-compose up (to start RabbitMQ)
 * - mvn spring-boot:run (to start this application)
 * - POST to http://localhost:8080/api/send?message=Hello (to send messages)
 */
@SpringBootApplication
public class RabbitMqTutorialApplication {

    public static void main(String[] args) {
        SpringApplication.run(RabbitMqTutorialApplication.class, args);
    }

}
