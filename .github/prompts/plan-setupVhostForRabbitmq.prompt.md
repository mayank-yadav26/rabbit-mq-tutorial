# Plan: Setup Custom Virtual Host for RabbitMQ

## Overview
Convert the RabbitMQ tutorial project from using the default vhost `/` to a custom vhost `/tutorial` with pre-configured queues, exchanges, and bindings.

## Current State
- RabbitMQ running with default vhost `/`
- All queues and exchanges in default `/` vhost
- Guest user with access to default vhost

## Desired State
- RabbitMQ with two vhosts: `/` (default) and `/tutorial` (custom)
- All tutorial resources in `/tutorial` vhost
- Guest user with permissions on both vhosts
- Automatic vhost setup on startup via definitions.json

## Files to Create/Modify

### 1. CREATE: definitions.json
**Location:** `/home/mayank/MyProjects/rabbit-mq-tutorial/definitions.json`

**Purpose:** Defines RabbitMQ configuration including:
- Virtual hosts (`/` and `/tutorial`)
- Users and permissions
- Queues (tutorial.queue in /tutorial)
- Exchanges (tutorial.exchange in /tutorial)
- Bindings between exchanges and queues

**Content Structure:**
```json
{
  "vhosts": [
    { "name": "/" },
    { "name": "/tutorial" }
  ],
  "users": [
    {
      "name": "guest",
      "password_hash": "...",
      "tags": "administrator"
    }
  ],
  "permissions": [
    { "user": "guest", "vhost": "/", "configure": ".*", "write": ".*", "read": ".*" },
    { "user": "guest", "vhost": "/tutorial", "configure": ".*", "write": ".*", "read": ".*" }
  ],
  "queues": [
    { "name": "tutorial.queue", "vhost": "/tutorial", "durable": true }
  ],
  "exchanges": [
    { "name": "tutorial.exchange", "vhost": "/tutorial", "type": "direct", "durable": true }
  ],
  "bindings": [
    { "source": "tutorial.exchange", "destination": "tutorial.queue", "vhost": "/tutorial", "routing_key": "tutorial.key" }
  ]
}
```

### 2. MODIFY: docker-compose.yml
**Location:** `/home/mayank/MyProjects/rabbit-mq-tutorial/docker-compose.yml`

**Changes:**
- Add volume mount for `definitions.json`
- Make definitions.json read-only (`:ro`)
- Keep existing certificate mounts

**New Volume:**
```yaml
- ./definitions.json:/etc/rabbitmq/definitions.json:ro
```

**Add after existing rabbitmq.conf volume mount**

### 3. MODIFY: rabbitmq.conf
**Location:** `/home/mayank/MyProjects/rabbit-mq-tutorial/rabbitmq.conf`

**Changes:**
- Uncomment or add: `management.load_definitions = /etc/rabbitmq/definitions.json`

**Purpose:** Tell RabbitMQ to load vhost definitions from the JSON file on startup

### 4. MODIFY: application.yml
**Location:** `/home/mayank/MyProjects/rabbit-mq-tutorial/src/main/resources/application.yml`

**Changes:**
- Add `virtual-host: /tutorial` under `spring.rabbitmq`

**New Configuration:**
```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5671
    username: guest
    password: guest
    virtual-host: /tutorial    # NEW LINE
    ssl:
      enabled: true
      # ... rest of SSL config
```

**Purpose:** Direct Spring Boot app to use `/tutorial` vhost instead of default `/`

## Implementation Steps

1. **Create definitions.json file**
   - Copy provided JSON content
   - Place in project root

2. **Update docker-compose.yml**
   - Add definitions.json volume mount
   - Restart Docker container

3. **Update rabbitmq.conf**
   - Uncomment management.load_definitions line
   - Verify path: `/etc/rabbitmq/definitions.json`

4. **Update application.yml**
   - Add virtual-host configuration
   - Recompile Spring Boot app

5. **Test Setup**
   ```bash
   # Restart RabbitMQ
   docker-compose down
   docker-compose up -d
   
   # Verify vhosts
   docker exec rabbitmq-mtls rabbitmqctl list_vhosts
   
   # Rebuild and run app
   mvn clean package
   mvn spring-boot:run
   ```

## Verification Checklist

- [ ] definitions.json created in project root
- [ ] docker-compose.yml has definitions.json volume mount
- [ ] rabbitmq.conf has management.load_definitions uncommented
- [ ] application.yml has virtual-host: /tutorial added
- [ ] RabbitMQ container restarted and healthy
- [ ] Spring Boot app starts without SSL errors
- [ ] `rabbitmqctl list_vhosts` shows both `/` and `/tutorial`
- [ ] RabbitMQ UI shows queues/exchanges under `/tutorial` vhost
- [ ] API test sends messages successfully
- [ ] Messages appear in `/tutorial` vhost queue

## Benefits

✅ Logical separation of tutorial resources
✅ Production-like vhost architecture
✅ Cleaner resource organization
✅ Easier to understand microservices pattern
✅ Default vhost remains clean for other uses
✅ Automatic setup on container startup

## Rollback Plan

If issues occur:
1. Remove definitions.json volume mount from docker-compose.yml
2. Comment out management.load_definitions in rabbitmq.conf
3. Remove virtual-host line from application.yml
4. Restart containers and app
5. Back to default vhost `/`

## Notes

- Password hash in definitions.json is for guest/guest
- Do NOT modify vhost names without updating application.yml
- definitions.json is read-only in Docker container
- Vhosts created on startup, existing on restart
- Each vhost has isolated queues, exchanges, and bindings
