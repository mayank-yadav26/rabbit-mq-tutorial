# Certificates Directory

This directory contains SSL/TLS certificates for mTLS authentication.

## Directory Structure

- `tls-gen/` - The RabbitMQ tls-gen tool (cloned by generate-certs.sh)
- `result/` - Generated certificates (created by generate-certs.sh)

## Generated Files

After running `../scripts/generate-certs.sh`, you'll find:

### PEM Certificates (Text Format - Human Readable)
- `ca_certificate.pem` - Root CA certificate (for verification)
- `server_certificate.pem` - RabbitMQ server certificate
- `server_key.pem` - RabbitMQ server private key
- `client_certificate.pem` - Java app client certificate
- `client_key.pem` - Java app client private key

### Java Keystores (Binary Format)
- `client_key.p12` - PKCS12 keystore with client cert + key (for Java)
- `ca_truststore.jks` - JKS truststore with CA certificate (for Java)

## Usage

1. Run certificate generation:
   ```bash
   ../scripts/generate-certs.sh
   ```

2. RabbitMQ Docker uses:
   - `server_certificate.pem`
   - `server_key.pem`
   - `ca_certificate.pem`

3. Spring Boot Java app uses:
   - `client_key.p12` (configured in application.yml as key-store)
   - `ca_truststore.jks` (configured in application.yml as trust-store)

## Security Notes

- **Never commit certificates to version control!** (Exception: CA cert for verification)
- **Private keys should be protected** with restricted file permissions (chmod 600)
- **Passwords should be stored securely** (not in code or committed files)
- **Use different certificates for each environment** (dev, staging, production)

## Further Reading

- [RabbitMQ SSL Documentation](https://www.rabbitmq.com/ssl.html)
