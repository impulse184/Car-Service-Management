# Car Operations Management System - Architecture Diagram & Layout

This document provides a standalone reference for the microservices architecture, network routing, security layers, and data flow of the Car Operations Management System.

---

## 🎨 System Architecture Graphic

![System Architecture Diagram](architecture_diagram.png)

---

## 📊 System Topology & Component Interactions

```mermaid
graph TD
    subgraph Client Layer
        UI["Web Frontend Dashboard"]
    end

    subgraph Edge Layer
        GW["API Gateway :8765<br/>(JWT Auth, RBAC, CORS, Swagger Aggregator)"]
    end

    subgraph Service Discovery
        EUK["Eureka Discovery Server :8761"]
    end

    subgraph Core Microservices Layer
        UP["User Profile Service :8081<br/>(Service-Level JWT Security Filter)"]
        OPS["Car Service Operations :8082<br/>(Service-Level JWT Security Filter)"]
        VAL["Car Details Validation Service :8083<br/>(Service-Level JWT Security Filter)"]
        AUD["Audit Logging Service :8084<br/>(Service-Level JWT Security Filter)"]
    end

    subgraph Persistence Layer
        DB_UP[("carservice_user_db")]
        DB_OPS[("carservice_ops_db")]
        DB_AUD[("carservice_audit_db")]
    end

    subgraph Event Streaming
        KFK["Apache Kafka Broker :9092<br/>(Topic: car-service-audit-events)"]
    end

    subgraph Actuator Health Monitoring
        ACT["Admin Actuator Panel<br/>(Live Status Cards & JSON Explorer)"]
    end

    %% Routing & Client Interactions
    UI -->|HTTP Requests| GW
    GW -->|Route /users| UP
    GW -->|Route /carservice| OPS
    GW -->|Route /audits| AUD

    %% Discovery
    UP -.->|Register & Heartbeat| EUK
    OPS -.->|Register & Heartbeat| EUK
    VAL -.->|Register & Heartbeat| EUK
    AUD -.->|Register & Heartbeat| EUK
    GW -.->|Lookup Microservices| EUK

    %% Inter-Service Communication via OpenFeign
    OPS -->|OpenFeign: Verify Customer| UP
    OPS -->|OpenFeign: Validate Plate Format| VAL

    %% Event-Driven Auditing via Kafka
    OPS -->|Publish Audit Event| KFK
    KFK -->|Consume Audit Event| AUD

    %% Persistence
    UP --> DB_UP
    OPS --> DB_OPS
    AUD --> DB_AUD

    %% Actuator Monitoring
    UI -.->|Monitor /actuator/health| ACT
    ACT -.->|Poll Health & Metrics| GW
    ACT -.->|Poll Direct Actuators| UP
    ACT -.->|Poll Direct Actuators| OPS
    ACT -.->|Poll Direct Actuators| VAL
    ACT -.->|Poll Direct Actuators| AUD
    ACT -.->|Poll Direct Actuators| EUK
```

---

## 🔑 Key Architecture Details

### 1. Dual-Layer Security
* **Layer 1 (API Gateway)**: Validates incoming JWT tokens, handles CORS preflight, enforces RBAC roles (`ADMIN`, `MECHANIC`, `CUSTOMER`), and injects `X-Authenticated-*` headers.
* **Layer 2 (Microservice Level)**: Downstream `ServiceSecurityFilter` and `JwtUtil` validate JWT `Authorization: Bearer <token>` or Gateway context headers. Unauthenticated direct access bypassing Gateway is rejected with `401 Unauthorized`.

### 2. Synchronous REST & Asynchronous Event Streaming
* **Spring Cloud OpenFeign**: `car-service-operations` synchronously verifies user accounts against `user-profile-service` and license plates against `car-details-validation-service`.
* **Apache Kafka**: `car-service-operations` publishes audit events to `car-service-audit-events`, which are consumed asynchronously by `audit-service`.

### 3. Spring Boot Actuator Monitoring
* All 6 microservices expose Spring Boot Actuator health indicators (`db`, `diskSpace`, `ping`, `discoveryComposite`).
* The Web App frontend features an Admin Health & Actuator Panel with live 🟢 **UP** / 🔴 **DOWN** status cards and an interactive JSON Explorer (`/health`, `/metrics`, `/env`, `/beans`, `/mappings`).
