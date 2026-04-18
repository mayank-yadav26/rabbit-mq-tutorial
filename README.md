# RabbitMQ mTLS Tutorial

A Spring Boot application demonstrating secure RabbitMQ communication using mutual TLS (mTLS) authentication with client certificates.

## Overview

This project demonstrates:
- **mTLS Authentication**: Both server and client authenticate each other using X.509 certificates
- **RabbitMQ Integration**: Spring AMQP for message publishing and consuming
- **Docker Compose**: Complete setup with RabbitMQ and SSL/TLS configuration
- **Certificate Generation**: Automated script using RabbitMQ's tls-gen tool

## Quick Start

### 1. Generate Certificates
```bash
./scripts/generate-certs.sh
```

This script:
- Downloads the RabbitMQ tls-gen tool (if not present)
- Generates CA, server, and client certificates
- Creates Java keystores (PKCS12 and JKS formats)
- Outputs all certificates to `certificates/result/`

### 2. Start RabbitMQ with mTLS
```bash
docker-compose up
```

RabbitMQ will:
- Listen on port 5671 (AMQP over TLS)
- Listen on port 15672 (Management UI)
- Use certificates from `certificates/result/`

### 3. Run the Spring Boot Application
```bash
mvn spring-boot:run
```

The application:
- Connects to RabbitMQ using mTLS
- Creates a queue named `tutorial.queue`
- Starts listening for messages

### 4. Send a Test Message
```bash
curl -X POST 'http://localhost:8080/api/send?message=Hello%20World'
```

Response:
```json
{"message":"Message sent successfully over mTLS","status":"success"}
```

## Architecture

```
┌─────────────────────────────────────────────────────┐
│  Spring Boot Application (port 8080)                │
│  ├─ ProducerController: REST API for messages       │
│  ├─ RabbitMQProducer: Sends messages to RabbitMQ    │
│  └─ RabbitMQConsumer: Receives messages             │
└────────────────┬────────────────────────────────────┘
                 │ mTLS (port 5671)
                 │ ├─ client_key.p12 (client cert+key)
                 │ └─ ca_truststore.jks (CA verification)
                 │
┌────────────────▼────────────────────────────────────┐
│  RabbitMQ with mTLS (Docker Container)              │
│  ├─ Port 5671: AMQP over SSL/TLS                    │
│  ├─ Port 15672: Management UI (HTTP)                │
│  └─ Certs:                                          │
│     ├─ server_certificate.pem                       │
│     ├─ server_key.pem                               │
│     └─ ca_certificate.pem                           │
└─────────────────────────────────────────────────────┘
```

## File Structure

```
rabbit-mq-tutorial/
├── src/main/java/com/example/rabbitmq/
│   ├── RabbitMqTutorialApplication.java
│   ├── config/RabbitMQConfig.java
│   ├── controller/ProducerController.java
│   ├── service/RabbitMQProducer.java
│   └── consumer/RabbitMQConsumer.java
├── src/main/resources/
│   ├── application.yml
│   ├── client_key.p12 (Java keystore - copied after cert generation)
│   └── ca_truststore.jks (Java truststore - copied after cert generation)
├── certificates/
│   ├── README.md
│   ├── result/ (Generated certificates - DO NOT COMMIT)
│   └── tls-gen/ (RabbitMQ tool - DO NOT COMMIT)
├── scripts/
│   ├── generate-certs.sh (Run this first!)
│   ├── quickstart.sh
│   └── verify-connection.sh
├── docker-compose.yml
├── rabbitmq.conf (RabbitMQ configuration)
└── pom.xml
```

## Configuration

### application.yml
- RabbitMQ host: `localhost`
- RabbitMQ port: `5671` (SSL/TLS)
- Keystore: `classpath:client_key.p12` (client authentication)
- Truststore: `classpath:ca_truststore.jks` (server verification)

### rabbitmq.conf
- SSL listener on port 5671
- mTLS enabled (mutual authentication)
- TLS 1.2 and 1.3 only
- Custom cipher suites

## API Endpoints

### Send Message
```
POST /api/send?message=<message_text>
```

Example:
```bash
curl -X POST 'http://localhost:8080/api/send?message=Hello'
```

## Troubleshooting

### Certificate Errors
If you see SSL handshake errors:
1. Regenerate certificates: `./scripts/generate-certs.sh`
2. Restart RabbitMQ: `docker-compose down && docker-compose up`
3. Rebuild application: `mvn clean package`

### Connection Refused
- Ensure RabbitMQ is running: `docker-compose ps`
- Check logs: `docker-compose logs -f rabbitmq-mtls`

### Port Already in Use
- RabbitMQ AMQP: port 5671
- RabbitMQ Management: port 15672
- Spring Boot: port 8080

## Certificate Generation Details

The `generate-certs.sh` script uses RabbitMQ's tls-gen tool to:

1. **Generate CA (Root Certificate)**
   - Self-signed for development
   - Valid for 10 years

2. **Generate Server Certificate**
   - Issued by CA
   - For RabbitMQ server authentication

3. **Generate Client Certificate**
   - Issued by CA
   - For Spring Boot application authentication

4. **Convert to Java Formats**
   - `client_key.p12`: PKCS12 keystore with client cert
   - `ca_truststore.jks`: JKS truststore with CA cert

## Security Notes

### For Development
- Certificates are self-signed (not trusted by browsers/clients)
- Passwords hardcoded in config (for demo purposes only)
- Certificate validity: 10 years

### For Production
- Use CA-signed certificates from trusted authority
- Store passwords in secure secret management (Vault, AWS Secrets Manager, etc.)
- Implement short-lived certificates with automatic rotation
- Use different certificates per environment
- Enable audit logging in RabbitMQ
- Implement rate limiting and connection timeouts

## Certificate Safety

**IMPORTANT**: The `.gitignore` file excludes:
```
certificates/result/     # Generated certificates
certificates/tls-gen/    # Downloaded tool
*.pem, *.p12, *.jks      # Certificate files
```

This prevents accidentally committing sensitive certificates to version control.

## Links

- [RabbitMQ SSL Documentation](https://www.rabbitmq.com/ssl.html)
- [RabbitMQ tls-gen Tool](https://github.com/rabbitmq/tls-gen)
- [Spring AMQP Documentation](https://spring.io/projects/spring-amqp)
- [Java SSL/TLS Documentation](https://docs.oracle.com/javase/tutorial/security/toolfilex/)

## License

MIT License

## Author

Created for learning RabbitMQ mTLS with Spring Boot
