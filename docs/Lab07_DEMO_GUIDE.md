# Spring Boot Actuator & Admin Server — Demo Guide

## Overview

This guide demonstrates how to run and verify Spring Boot Actuator and Spring Boot Admin Server within a microservice architecture.

## Prerequisites & Startup

### Start the system

```bash
docker-compose up -d
```

Wait until all services are healthy (verify with `docker-compose ps`).

### Service endpoints

* **Eureka Server**: [http://localhost:8761](http://localhost:8761)
* **Config Server**: [http://localhost:8888](http://localhost:8888)
* **Admin Server**: [http://localhost:9090](http://localhost:9090)
* **Gateway Service**: [http://localhost:8080](http://localhost:8080)
* **Order Service**: [http://localhost:8081](http://localhost:8081)
* **Inventory Service**: [http://localhost:8082](http://localhost:8082)
* **Shipping Service**: [http://localhost:8083](http://localhost:8083)
* **Analytics Service**: [http://localhost:8084](http://localhost:8084)

## Spring Boot Admin UI

### Access

Open **[http://localhost:9090](http://localhost:9090)**.

### Expected UI

* Landing page lists all registered applications.
* Each service shows **UP** (green) status.
* Visible applications: `order-service`, `inventory-service`, `shipping-service`, `analytics-service`, `gateway-service`, `config-server`, `eureka-server`.

### Inspect an application

1. Select an application (e.g., `order-service`).
2. Review tabs:

    * **Health** — application and dependency health.
    * **Metrics** — JVM, HTTP, memory, and related metrics.
    * **Env** — environment and configuration.
    * **Beans** — Spring beans list.
    * **Loggers** — logger configuration.
    * **Threads** — thread information.

### Metrics to verify

In **Metrics** tab, ensure presence of:

* `jvm.memory.used`
* `http.server.requests`
* `process.uptime`

## Actuator API (via curl)

### Health check

```bash
# Order Service
curl -s http://localhost:8081/actuator/health | jq

# Gateway Service
curl -s http://localhost:8080/actuator/health | jq

# Admin Server
curl -s http://localhost:9090/actuator/health | jq
```

**Expected (example):**

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP", "details": { "database": "PostgreSQL", "validationQuery": "isValid()" } },
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

### JVM metrics

```bash
# Order Service
curl -s http://localhost:8081/actuator/metrics/jvm.memory.used | jq

# Inventory Service
curl -s http://localhost:8082/actuator/metrics/jvm.memory.used | jq
```

**Expected (shape):**

```json
{
  "name": "jvm.memory.used",
  "description": "The amount of used memory",
  "baseUnit": "bytes",
  "measurements": [{ "statistic": "VALUE", "value": 123456789 }],
  "availableTags": ["..."]
}
```

### Application info

```bash
# Order Service
curl -s http://localhost:8081/actuator/info | jq

# Gateway Service
curl -s http://localhost:8080/actuator/info | jq
```

**Expected (example):**

```json
{
  "app": {
    "name": "order-service",
    "version": "0.0.1-SNAPSHOT",
    "env": "docker"
  }
}
```

### List available Actuator endpoints

```bash
# Order Service
curl -s http://localhost:8081/actuator | jq

# Gateway Service
curl -s http://localhost:8080/actuator | jq
```

### Via Gateway (if proxied)

```bash
# Health through Gateway
curl -s http://localhost:8080/actuator/health | jq

# Info through Gateway
curl -s http://localhost:8080/actuator/info | jq
```

## Eureka Dashboard

### Registration verification

Open **[http://localhost:8761](http://localhost:8761)** and confirm **Instances currently registered with Eureka** include:

* `ADMIN-SERVER`
* `CONFIG-SERVER`
* `GATEWAY-SERVICE`
* `ORDER-SERVICE`
* `INVENTORY-SERVICE`
* `SHIPPING-SERVICE`
* `ANALYTICS-SERVICE`

## Verification Checklist

* [ ] All services respond to `/actuator/health`.
* [ ] All services are visible in Eureka Dashboard.
* [ ] All services appear in Spring Boot Admin UI with **UP** status.
* [ ] Metrics and environment are accessible in the Admin UI.
* [ ] Health endpoint shows detailed info (`show-details: always`).
* [ ] Info endpoint returns application metadata.

## Troubleshooting

### Services not visible in Admin UI

1. Ensure Eureka Server is running and reachable.
2. Verify services are registered in Eureka.
3. Check Admin Server logs: `docker logs admin-server`.

### Actuator endpoints not reachable

1. Confirm `management.endpoints.web.exposure.include: "*"` is configured.
2. Ensure the service is running and the base URL is correct.
3. Inspect service logs: `docker logs <service-name>`.

### Health shows DOWN

1. Verify database connectivity.
2. Review service logs for errors.
3. Ensure required dependencies (e.g., PostgreSQL, Artemis) are running.

## Additional Commands

### List all metrics

```bash
curl -s http://localhost:8081/actuator/metrics | jq
```

### Query specific metrics

```bash
curl -s http://localhost:8081/actuator/metrics/jvm.memory.max | jq
curl -s http://localhost:8081/actuator/metrics/http.server.requests | jq
```

### Environment

```bash
curl -s http://localhost:8081/actuator/env | jq
```

### Loggers

```bash
curl -s http://localhost:8081/actuator/loggers | jq
```
