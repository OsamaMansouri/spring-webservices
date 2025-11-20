# Spring Web Services - REST API Comparison Project

A comprehensive Spring Boot project that demonstrates and benchmarks three different approaches to building REST web services:

1. **JAX-RS (Jersey)** - Standard Jakarta EE REST API implementation
2. **Spring @RestController** - Native Spring MVC REST controllers
3. **Spring Data REST** - Automatic REST API generation from repositories

**Auteurs : Sohaib Laarichi & Osama Mansouri**


## 📋 Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)
- [Running the Application](#running-the-application)
- [Monitoring Setup](#monitoring-setup)
- [Performance Benchmarks](#performance-benchmarks)
- [Project Structure](#project-structure)
- [Technologies Used](#technologies-used)
- [Recommendations](#recommendations)
- [Documentation](#documentation)

## ✨ Features

- **Three REST API Implementations** running simultaneously
- **CRUD Operations** for Items and Categories
- **Performance Benchmarks** with detailed metrics
- **Monitoring Stack** (Prometheus + Grafana)
- **Test Data Generation** (2,000 categories, 100,000 items)
- **JMeter Test Plans** for load testing
- **Comprehensive Comparison Report** with recommendations

## 🏗️ Architecture

### Three API Implementations

| Approach | Base Path | Configuration | Lines of Code |
|----------|-----------|---------------|---------------|
| **JAX-RS (Jersey)** | `/api/*` | `spring.jersey.application-path=/api` | ~83 lines |
| **Spring @RestController** | `/api-rest/*` | Auto-discovered | ~55 lines |
| **Spring Data REST** | `/api-data-rest/*` | `spring.data.rest.base-path=/api-data-rest` | ~6 lines |

### Technology Stack

```
┌─────────────────────────────────────┐
│   Spring Boot Application (8087)    │
│  ┌──────────┐ ┌──────────┐ ┌──────┐│
│  │  JAX-RS  │ │  REST    │ │ Data ││
│  │  Jersey  │ │Controller│ │ REST ││
│  └────┬─────┘ └────┬─────┘ └──┬───┘│
│       └────────────┼───────────┘    │
│                    │                │
│         ┌──────────▼──────────┐     │
│         │  Spring Data JPA    │     │
│         └──────────┬──────────┘     │
└────────────────────┼─────────────────┘
                     │
          ┌──────────▼──────────┐
          │   PostgreSQL (5454) │
          └─────────────────────┘

┌─────────────────────────────────────┐
│      Monitoring Stack (Docker)      │
│  ┌────────────┐  ┌──────────────┐  │
│  │ Prometheus │  │   Grafana    │  │
│  │  (9090)    │  │   (3000)     │  │
│  └────────────┘  └──────────────┘  │
└─────────────────────────────────────┘
```

## 📦 Prerequisites

- **Java 17+** (Project uses Java 17, but tested with JDK 21)
- **Maven 3.6+**
- **PostgreSQL 15+** (or Docker)
- **Docker & Docker Compose** (for monitoring stack)
- **Apache JMeter** (optional, for load testing)

## 🚀 Installation

### 1. Clone the Repository

```bash
git clone <repository-url>
cd spring-webservices
```

### 2. Database Setup

#### Option A: Local PostgreSQL

1. Install PostgreSQL 15+
2. Create database:
```sql
CREATE DATABASE jaxrs;
CREATE USER postgres WITH PASSWORD '0000';
GRANT ALL PRIVILEGES ON DATABASE jaxrs TO postgres;
```

3. Update `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5454/jaxrs
```

#### Option B: Docker PostgreSQL

1. Start PostgreSQL container:
```bash
docker-compose up -d postgres
```

2. Update `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5455/jaxrs
```

### 3. Generate Test Data

Connect to PostgreSQL and run:

```bash
psql -h localhost -p 5454 -U postgres -d jaxrs -f data/generate-test-data.sql
```

This will create:
- **2,000 categories** (CAT-00001 to CAT-02000)
- **100,000 items** (SKU-00000001 to SKU-00010000)
- ~50 items per category

### 4. Build the Project

```bash
mvn clean install
```

## ⚙️ Configuration

### Application Properties

Edit `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5454/jaxrs
spring.datasource.username=postgres
spring.datasource.password=0000

# Server Port
server.port=8087

# JAX-RS Configuration
spring.jersey.application-path=/api

# Spring Data REST Configuration
spring.data.rest.base-path=/api-data-rest
spring.data.rest.default-page-size=20
spring.data.rest.max-page-size=100

# Actuator Configuration
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.prometheus.enabled=true
```

### Enable/Disable API Implementations

**Disable JAX-RS:**
```properties
# Comment out or remove this line:
# spring.jersey.application-path=/api
```

**Disable Spring Data REST:**
```properties
# Comment out these lines:
# spring.data.rest.base-path=/api-data-rest
# spring.data.rest.default-page-size=20
# spring.data.rest.max-page-size=100
```

**Disable REST Controllers:**
- Remove or comment `@RestController` annotations in controller classes

## 📡 API Endpoints

### JAX-RS (Jersey) - `/api/*`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/items` | Get all items (paginated) |
| GET | `/api/items?categoryId={id}` | Get items by category |
| GET | `/api/items/{id}` | Get item by ID |
| POST | `/api/items` | Create new item |
| PUT | `/api/items/{id}` | Update item |
| DELETE | `/api/items/{id}` | Delete item |
| GET | `/api/categories` | Get all categories (paginated) |
| GET | `/api/categories/{id}` | Get category by ID |
| GET | `/api/categories/{id}/items` | Get items by category |
| POST | `/api/categories` | Create new category |
| PUT | `/api/categories/{id}` | Update category |
| DELETE | `/api/categories/{id}` | Delete category |

**Features:**
- ✅ Support for JSON and XML (`Accept: application/xml`)
- ✅ Standard JAX-RS annotations

### Spring @RestController - `/api-rest/*`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api-rest/items` | Get all items (paginated) |
| GET | `/api-rest/items?categoryId={id}` | Get items by category |
| GET | `/api-rest/items/{id}` | Get item by ID |
| POST | `/api-rest/items` | Create new item |
| PUT | `/api-rest/items/{id}` | Update item |
| DELETE | `/api-rest/items/{id}` | Delete item |
| GET | `/api-rest/categories` | Get all categories (paginated) |
| GET | `/api-rest/categories/{id}` | Get category by ID |
| GET | `/api-rest/categories/{id}/items` | Get items by category |
| POST | `/api-rest/categories` | Create new category |
| PUT | `/api-rest/categories/{id}` | Update category |
| DELETE | `/api-rest/categories/{id}` | Delete category |

**Features:**
- ✅ Native Spring integration
- ✅ Easy validation with `@Valid`
- ✅ Flexible error handling with `@ControllerAdvice`

### Spring Data REST - `/api-data-rest/*`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api-data-rest/items` | Get all items (HAL format) |
| GET | `/api-data-rest/items?page=0&size=10` | Pagination |
| GET | `/api-data-rest/items?sort=price,asc` | Sorting |
| GET | `/api-data-rest/items/search/byCategory?categoryId={id}` | Custom search |
| GET | `/api-data-rest/items/{id}` | Get item by ID (HAL format) |
| POST | `/api-data-rest/items` | Create new item |
| PUT | `/api-data-rest/items/{id}` | Update item |
| PATCH | `/api-data-rest/items/{id}` | Partial update |
| DELETE | `/api-data-rest/items/{id}` | Delete item |
| GET | `/api-data-rest/categories` | Get all categories |
| GET | `/api-data-rest/profile/items` | Get metadata |

**Features:**
- ✅ Automatic API generation
- ✅ HAL JSON format (HATEOAS)
- ✅ Built-in pagination, sorting, and search
- ✅ Automatic projections

### Example Requests

#### Create an Item (JAX-RS)

```bash
curl -X POST http://localhost:8087/api/items \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "sku": "SKU-TEST-001",
    "name": "Test Item",
    "price": 99.99,
    "stock": 100,
    "category": {"id": 1}
  }'
```

#### Get Items with Pagination (REST Controller)

```bash
curl "http://localhost:8087/api-rest/items?page=0&size=20&categoryId=1"
```

#### Get Items (Spring Data REST - HAL Format)

```bash
curl "http://localhost:8087/api-data-rest/items?page=0&size=10"
```

## 🏃 Running the Application

### Start the Application

```bash
mvn spring-boot:run
```

Or build and run the JAR:

```bash
mvn clean package
java -jar target/Jaxrs-0.0.1-SNAPSHOT.jar
```

### Verify it's Running

- **Health Check**: http://localhost:8087/actuator/health
- **Metrics**: http://localhost:8087/actuator/metrics
- **Prometheus**: http://localhost:8087/actuator/prometheus

### Test Endpoints

```bash
# Test JAX-RS
curl http://localhost:8087/api/items?page=0&size=5

# Test REST Controller
curl http://localhost:8087/api-rest/items?page=0&size=5

# Test Spring Data REST
curl http://localhost:8087/api-data-rest/items?page=0&size=5
```

## 📊 Monitoring Setup

### Start Monitoring Stack

```bash
docker-compose up -d
```

This starts:
- **PostgreSQL** (port 5455)
- **Prometheus** (port 9090)
- **Grafana** (port 3000)

### Access Monitoring Tools

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000
  - Username: `admin`
  - Password: `admin`

### Grafana Dashboard

The project includes a pre-configured Grafana dashboard:
- **Location**: `data/grafana/dashboard/spring-boot-statistics.json`
- **Metrics**: CPU, Memory, Threads, GC, RPS, Error Rate, etc.

### Import Dashboard

1. Open Grafana: http://localhost:3000
2. Go to **Dashboards** → **Import**
3. Upload `data/grafana/dashboard/spring-boot-statistics.json`
4. Select Prometheus as data source

## 📈 Performance Benchmarks

The project includes comprehensive performance benchmarks comparing all three implementations across four scenarios:

### Test Scenarios

1. **HEAVY-body**: Large payloads (5 KB) for POST/PUT operations
2. **JOIN-filter**: Complex JOIN queries with category filtering
3. **MIXED**: Mix of CRUD operations (GET/POST/PUT/DELETE)
4. **READ-heavy**: Read-only workload with high concurrency

### Key Results Summary

| Scenario | Best RPS | Best Latency (p95) | Best Error Rate | Winner |
|----------|----------|-------------------|-----------------|---------|
| **HEAVY-body** | 296.7 (Data REST) | 264ms (REST Ctrl) | 0% (Data REST) | REST Controllers |
| **JOIN-filter** | 149.5 (REST Ctrl) | 755ms (REST Ctrl) | 0% (All) | REST Controllers |
| **MIXED** | 317.8 (JAX-RS) | 336ms (JAX-RS) | 0.09% (JAX-RS) | JAX-RS |
| **READ-heavy** | 68.0 (JAX-RS) | 1,260ms (REST Ctrl) | 0% (REST Ctrl) | REST Controllers |

### Detailed Metrics

See `RAPPORT_COMPARAISON_WEB_SERVICES.md` for complete benchmark results including:
- Throughput (RPS)
- Latency (p50, p95, p99)
- Error rates
- CPU and memory usage
- GC pause times
- Thread utilization

### Running JMeter Tests

JMeter test plans are located in `jmeter/` directory:

```bash
# Example: Run HEAVY-body scenario for JAX-RS
jmeter -n -t jmeter/jaxrs/heavy-body-scenario.jmx \
  -l results.jtl \
  -e -o report/
```

**Test Plans Available:**
- `jmeter/jaxrs/*.jmx` - JAX-RS tests
- `jmeter/rest/*.jmx` - REST Controller tests
- `jmeter/spring-data-rest/*.jmx` - Spring Data REST tests

## 📁 Project Structure

```
spring-webservices/
├── src/
│   ├── main/
│   │   ├── java/com/example/Jaxrs/
│   │   │   ├── config/
│   │   │   │   └── MyConfig.java          # JAX-RS configuration
│   │   │   ├── controller/
│   │   │   │   ├── ItemJaxrsApi.java      # JAX-RS controller
│   │   │   │   ├── ItemRestController.java # REST controller
│   │   │   │   ├── CategoryJaxrsApi.java
│   │   │   │   └── CategoryRestController.java
│   │   │   ├── entity/
│   │   │   │   ├── Item.java
│   │   │   │   └── Category.java
│   │   │   ├── repository/
│   │   │   │   ├── ItemRepository.java    # Spring Data REST enabled
│   │   │   │   └── CategoryRepository.java
│   │   │   └── JaxrsApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── data/
│   ├── generate-test-data.sql             # Test data generation script
│   ├── grafana/                           # Grafana dashboards and config
│   ├── postgres/                          # PostgreSQL data directory
│   └── prometheus/                        # Prometheus configuration
├── jmeter/                                # JMeter test plans and results
│   ├── jaxrs/
│   ├── rest/
│   └── spring-data-rest/
├── docker-compose.yaml                    # Docker services configuration
├── pom.xml                                # Maven dependencies
├── RAPPORT_COMPARAISON_WEB_SERVICES.md    # Detailed comparison report
├── DASHBOARD_NOTES.md                     # Grafana dashboard notes
└── README.md                              # This file
```

## 🛠️ Technologies Used

### Core Framework
- **Spring Boot 3.5.7**
- **Spring Data JPA**
- **Spring MVC**
- **Spring Data REST**

### REST APIs
- **Jersey (JAX-RS 3.1.1)**
- **Jakarta XML Binding (JAXB 4.0)**

### Database
- **PostgreSQL 15**
- **HikariCP** (connection pool)

### Monitoring
- **Spring Boot Actuator**
- **Micrometer**
- **Prometheus**
- **Grafana**

### Testing
- **Apache JMeter**
- **JUnit**

### Build Tools
- **Maven**
- **Lombok**

## 💡 Recommendations

Based on the comprehensive benchmarks, here are the recommendations:

### 🥇 **Spring @RestController** - Best Overall Choice

**Use when:**
- ✅ Developing Spring Boot applications
- ✅ Need full control over endpoints
- ✅ Require optimal performance and stability
- ✅ Need easy integration with Spring Security, validation, etc.

**Advantages:**
- Lowest CPU usage (5-11%)
- Lowest error rate (0% in most scenarios)
- Best latency for most operations
- Native Spring integration
- Excellent for complex business logic

### 🥈 **JAX-RS (Jersey)** - Excellent Alternative

**Use when:**
- ✅ Need native XML support
- ✅ Working in multi-server environments
- ✅ Require Jakarta EE standard compliance
- ✅ Need portability across application servers

**Advantages:**
- Native XML support (no extra configuration)
- Standard Jakarta EE
- Best throughput for mixed workloads (317.8 RPS)
- PUT/DELETE operations work correctly in all tests

### 🥉 **Spring Data REST** - Specific Use Cases

**Use when:**
- ✅ Simple CRUD operations
- ✅ Rapid API development
- ✅ Need automatic pagination, sorting, search
- ✅ HAL JSON format is acceptable

**⚠️ Limitations:**
- PUT/DELETE operations: 100% error rate in MIXED scenario
- High latency with relations (6.9s for GET /categories/{id})
- Requires special configuration for complex relations
- Higher CPU usage (up to 79%)

**Not recommended for:**
- Complex business logic
- High-performance requirements
- APIs with complex relationships

## 📚 Documentation

### Detailed Reports

- **[RAPPORT_COMPARAISON_WEB_SERVICES.md](RAPPORT_COMPARAISON_WEB_SERVICES.md)** - Complete comparison report (2095 lines)
  - Architecture analysis
  - Code examples
  - Performance benchmarks
  - Detailed metrics tables
  - Migration guides
  - Use case recommendations

- **[DASHBOARD_NOTES.md](DASHBOARD_NOTES.md)** - Grafana dashboard configuration
  - Prometheus query fixes
  - Metric explanations
  - Troubleshooting tips

### Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Jersey User Guide](https://eclipse-ee4j.github.io/jersey/)
- [Spring Data REST Documentation](https://docs.spring.io/spring-data/rest/docs/current/reference/html/)
- [JAX-RS Specification](https://jakarta.ee/specifications/restful-ws/)

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📝 License

This project is for educational and comparison purposes.

## 👤 Author

Spring Web Services Comparison Project

---

**Last Updated**: 2025  
**Version**: 1.0  
**Spring Boot Version**: 3.5.7  
**Java Version**: 17+

