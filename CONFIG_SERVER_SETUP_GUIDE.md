# Config Server Setup Guide

Vova, my lil’ legend, for you

This guide explains how to properly configure Spring Cloud Config Server in IntelliJ IDEA for local development.

## Overview

Spring Cloud Config Server provides centralized configuration management for microservices. It uses the `native` profile to read configuration files from the local filesystem.

## Prerequisites

- Java 17 SDK
- IntelliJ IDEA
- All services configured to use Config Server
- `config-repo` folder with YAML configuration files

## IntelliJ IDEA Run Configuration Setup

### Step 1: Create Run Configuration

1. Open IntelliJ IDEA
2. Go to **Run** → **Edit Configurations...**
3. Click **+** → **Spring Boot**
4. Name it: `ConfigServerApplication`

### Step 2: Configure Basic Settings

**Name:** `ConfigServerApplication`

**Main class:** `com.example.configserver.ConfigServerApplication`

**Java SDK:** Select `java 17 SDK` (or your project SDK)

**Module:** Select `ecommerce-platform.services.config-server.main`

### Step 3: Configure VM Options

This is the **most important step**. Add the following VM options:

#### Option A: Absolute Path (Recommended)

```
-Dspring.profiles.active=native
-Dspring.cloud.config.server.native.search-locations=file:/E:/Academy%20of%20Mohyla/Course3_Stage1/spring-boot-microservices/ecommerce-platform/config-repo
```

**Important:**
- Replace the path with your actual absolute path to `config-repo`
- Use forward slashes `/` (not backslashes `\`)
- Encode spaces as `%20` (e.g., `Academy%20of%20Mohyla`)
- Use `file:/` prefix for Windows absolute paths

#### Option B: Relative Path (If Working Directory = Project Root)

If your **Working Directory** is set to the project root (`ecommerce-platform`):

```
-Dspring.profiles.active=native
-Dspring.cloud.config.server.native.search-locations=file:./config-repo
```

#### Option C: Relative Path (If Working Directory = Module)

If your **Working Directory** is set to `services/config-server`:

```
-Dspring.profiles.active=native
-Dspring.cloud.config.server.native.search-locations=file:../../config-repo
```

### Step 4: Set Working Directory (Optional)

**Recommended:** Set **Working directory** to project root:
```
E:\Academy of Mohyla\Course3_Stage1\spring-boot-microservices\ecommerce-platform
```

This allows using relative paths like `file:./config-repo`.

### Step 5: Active Profiles

**Active profiles:** Leave empty or set to `native` (VM options already set it)

### Step 6: Additional Options

- ✅ **Open run/debug tool window when started** (recommended)
- ✅ **Add dependencies with "provided" scope to classpath** (recommended)

Click **Apply** and **OK**.

## Verification

### Step 1: Start Config Server

1. Run `ConfigServerApplication` from IntelliJ IDEA
2. Wait for startup to complete
3. Check logs for: `Started ConfigServerApplication`

### Step 2: Verify Configuration Files Are Loaded

Test the Config Server endpoints:

```powershell
# Check application default config
Invoke-WebRequest -Uri "http://localhost:8888/application/default" -UseBasicParsing

# Check gateway-service config
Invoke-WebRequest -Uri "http://localhost:8888/gateway-service/default" -UseBasicParsing

# Check order-service config
Invoke-WebRequest -Uri "http://localhost:8888/order-service/default" -UseBasicParsing
```

**Expected Response:**
```json
{
  "name": "gateway-service",
  "profiles": ["default"],
  "propertySources": [
    {
      "name": "file:/E:/.../config-repo/gateway-service.yml",
      "source": { ... }
    }
  ]
}
```

**If `propertySources` is empty:** The path in VM options is incorrect.

### Step 3: Check Config Server UI

Open: http://localhost:8888

You should see the Config Server UI with available endpoints.

## Client Services Configuration

All client services must be configured to use Config Server.

### Required Configuration

Each service's `application.yml` should contain:

```yaml
spring:
  application:
    name: <service-name>  # Must match config-repo filename
  config:
    import: "optional:configserver:"
  cloud:
    config:
      uri: http://localhost:8888
```

**Service names and their config files:**
- `gateway-service` → `config-repo/gateway-service.yml`
- `order-service` → `config-repo/order-service.yml`
- `inventory-service` → `config-repo/inventory-service.yml`
- `shipping-service` → `config-repo/shipping-service.yml`
- `analytics-service` → `config-repo/analytics-service.yml`
- `eureka-server` → `config-repo/eureka-server.yml`

### Client Service VM Options (Optional)

Client services don't require VM options if `application.yml` is configured correctly. However, you can override via VM options:

```
-Dspring.config.import=optional:configserver:
-Dspring.cloud.config.uri=http://localhost:8888
```

## Troubleshooting

### Issue: `propertySources: []` in Config Server Response

**Causes:**
- Incorrect path in VM options
- Path contains spaces not properly encoded
- Working Directory not set correctly

**Solutions:**
1. Use absolute path with `%20` for spaces
2. Verify path exists: Check `config-repo` folder location
3. Use forward slashes `/` instead of backslashes `\`
4. Ensure path format: `file:/E:/path/to/config-repo`

### Issue: Config Server Cannot Start

**Error:** `ClassNotFoundException` or path-related errors

**Solutions:**
1. Check VM options syntax (no quotes needed)
2. Verify Java SDK is selected
3. Check main class is correct
4. Ensure `config-repo` folder exists

### Issue: Client Services Cannot Connect to Config Server

**Error:** `Connection refused` or `Unable to find instance`

**Solutions:**
1. Ensure Config Server is running on port 8888
2. Verify `spring.cloud.config.uri=http://localhost:8888` in client config
3. Check `spring.config.import=optional:configserver:` is present
4. Restart client services after Config Server is running

### Issue: Spaces in Path Cause Errors

**Error:** `ClassNotFoundException: of` or similar

**Solution:** Encode spaces in VM options:
```
# Wrong:
-Dspring.cloud.config.server.native.search-locations=file:/E:/Academy of Mohyla/...

# Correct:
-Dspring.cloud.config.server.native.search-locations=file:/E:/Academy%20of%20Mohyla/...
```

### Issue: Port Already in Use

**Error:** `Port 8888 is already in use`

**Solution:**
1. Stop other instances of Config Server
2. Or change port in `application.yml`:
   ```yaml
   server:
     port: 8889
   ```
3. Update client services `spring.cloud.config.uri` accordingly

## Path Format Reference

### Windows Absolute Path

```
file:/E:/Academy%20of%20Mohyla/Course3_Stage1/spring-boot-microservices/ecommerce-platform/config-repo
```

**Rules:**
- Start with `file:/`
- Use forward slashes `/`
- Encode spaces as `%20`
- Include full path to `config-repo` folder

### Relative Path (Working Directory = Project Root)

```
file:./config-repo
```

### Relative Path (Working Directory = Module)

```
file:../../config-repo
```

## Startup Order

1. **Config Server** (port 8888) - Must start first
2. **Eureka Server** (port 8761) - After Config Server
3. **Client Services** - After Eureka Server:
   - Gateway Service (8080)
   - Order Service (8081)
   - Inventory Service (8082)
   - Shipping Service (8083)
   - Analytics Service (8084)

## Best Practices

1. **Always use absolute paths** in VM options for Config Server to avoid path-related issues
2. **Verify Config Server is working** before starting client services
3. **Use `optional:configserver:`** in client configs to allow services to start even if Config Server is temporarily unavailable
4. **Check logs** for configuration loading errors
5. **Test endpoints** after Config Server startup to verify configuration files are loaded

## Quick Reference

**Config Server VM Options (Recommended):**
```
-Dspring.profiles.active=native
-Dspring.cloud.config.server.native.search-locations=file:/E:/Academy%20of%20Mohyla/Course3_Stage1/spring-boot-microservices/ecommerce-platform/config-repo
```

**Config Server Port:** 8888

**Config Server URL:** http://localhost:8888

**Test Endpoint:** http://localhost:8888/{service-name}/default

**Config Files Location:** `config-repo/` folder in project root

