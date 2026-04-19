# mTLS Certificate Configuration Guide

## Understanding Keystore vs Truststore

### **What is Keystore?**
- **Contains**: Client's private key + client certificate
- **Protects**: Client's identity and private key
- **Usage**: When client needs to prove who it is to the server
- **Format in this project**: PKCS12 (.p12)
- **File**: `client_key.p12`

```
┌─────────────────────────────────┐
│    KEYSTORE (client_key.p12)    │
├─────────────────────────────────┤
│ • Client Private Key (SECRET)   │
│ • Client Certificate            │
└─────────────────────────────────┘
       ↓ Used for mTLS ↓
   Client authenticates to server
```

### **What is Truststore?**
- **Contains**: Certificate Authority (CA) certificate
- **Protects**: Trust chain verification
- **Usage**: When client needs to verify server is trustworthy
- **Format in this project**: JKS (.jks)
- **File**: `ca_truststore.jks`

```
┌─────────────────────────────────┐
│  TRUSTSTORE (ca_truststore.jks) │
├─────────────────────────────────┤
│ • CA Certificate (PUBLIC)       │
└─────────────────────────────────┘
       ↓ Used for mTLS ↓
 Client verifies server certificate
```

---

## Comparison: SEPARATE vs COMBINED

### **Current Setup: SEPARATE (Recommended for Security)**

```yaml
spring:
  rabbitmq:
    ssl:
      key-store: classpath:client_key.p12          # Client auth
      key-store-type: PKCS12
      key-store-password: password
      
      trust-store: classpath:ca_truststore.jks    # Server verification
      trust-store-type: JKS
      trust-store-password: password
```

**Advantages:**
✅ Better security - private key and public certs are separated
✅ Different formats allow format-specific optimization
✅ Follows security best practices
✅ Easier access control (can restrict keystore differently)
✅ Private key stays isolated

**Disadvantages:**
❌ Two files to manage
❌ Two passwords to configure
❌ More complex file management

---

### **Alternative: COMBINED (Like HA Certificates)**

```yaml
spring:
  rabbitmq:
    ssl:
      key-store: classpath:combined.p12           # Both in one file
      key-store-type: PKCS12
      key-store-password: password
      
      trust-store: classpath:combined.p12         # Same file
      trust-store-type: PKCS12
      trust-store-password: password
```

**Advantages:**
✅ Single file to manage
✅ One password
✅ Simpler configuration
✅ Similar to HA certificate approach
✅ Easier deployment

**Disadvantages:**
❌ Less security isolation
❌ Private key and CA cert in same file
❌ Not following strict security best practices
❌ Risk of exposing CA cert if keystore leaked

---

## How to Create Combined Certificate

### **Step 1: Extract CA Certificate from Truststore**
```bash
keytool -exportcert \
  -alias ca \
  -file ca_cert.pem \
  -keystore ca_truststore.jks \
  -storepass password \
  -rfc
```

### **Step 2: Create Combined PKCS12 File**
```bash
openssl pkcs12 -export \
  -in client_certificate.pem \
  -inkey client_key.pem \
  -certfile ca_certificate.pem \
  -out combined.p12 \
  -name rabbitmq-client \
  -passout pass:password
```

### **Step 3: Verify Combined File**
```bash
keytool -list -v -keystore combined.p12 -storepass password
```

This will show:
```
Your keystore contains 2 entries:

Alias name: rabbitmq-client
  Entry type: PrivateKeyEntry
  Certificate chain length: 1
  ...

Alias name: ca
  Entry type: trustedCertEntry
  ...
```

---

## Migration Guide: From Separate to Combined

### **IF you want to use combined certificate:**

1. **Create combined.p12** (using steps above)

2. **Replace in application.yml:**
```yaml
# Change FROM this:
ssl:
  key-store: classpath:client_key.p12
  trust-store: classpath:ca_truststore.jks

# Change TO this:
ssl:
  key-store: classpath:combined.p12
  key-store-type: PKCS12
  trust-store: classpath:combined.p12
  trust-store-type: PKCS12
```

3. **Place combined.p12 in** `src/main/resources/`

4. **Rebuild and run:**
```bash
mvn clean compile
mvn spring-boot:run
```

---

## How Certificate Verification Works in mTLS

```
Client                                    RabbitMQ Server
  │                                            │
  ├─────── 1. Client Hello ────────────────────>
  │
  <─── 2. Server Cert (signed by CA) ──────────┤
  │
  │ 3. Verify using TRUSTSTORE (CA cert)
  │    ✅ Server cert is signed by trusted CA
  │
  ├─────── 4. Send Client Cert + Key ────────────>
  │         (from KEYSTORE)
  │
  │ 5. Server verifies client cert
  │    ✅ Client authenticated
  │
  ├─────── 6. Encrypted Communication ────────────>
  │         mTLS tunnel established ✅
```

---

## Current Configuration in This Project

**File**: `src/main/resources/application.yml`
**Current Setup**: SEPARATE (more secure)

- **Keystore**: `client_key.p12` - Contains client's private key
- **Truststore**: `ca_truststore.jks` - Contains CA certificate
- **Connection**: mTLS with mutual authentication

---

## Security Recommendation

| Setup | Security | Simplicity | Recommendation |
|-------|----------|-----------|-----------------|
| Separate | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | **PRODUCTION** ✅ |
| Combined | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | **DEVELOPMENT** |

**Recommendation**: Keep **SEPARATE** certificates for production environments. Use **COMBINED** only for development/HA setups where management simplicity is prioritized.

---

## Troubleshooting

### Common Error: Certificate verification failed
- Check truststore has CA certificate
- Verify CA certificate is not expired
- Ensure server certificate is signed by the CA in truststore

### Common Error: Private key is expired
- Check keystore certificate expiration
- Re-generate certificates if expired

### Common Error: Wrong password
- Verify keystore password matches `key-store-password`
- Verify truststore password matches `trust-store-password`
