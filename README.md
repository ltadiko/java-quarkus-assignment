# Fulfilment Cost Control System - Code Assignment

[![Java CI with Maven](https://github.com/ltadiko/java-quarkus-assignment/actions/workflows/ci.yml/badge.svg)](https://github.com/ltadiko/java-quarkus-assignment/actions/workflows/ci.yml)

## 📋 About the Assignment

This is a Java/Quarkus-based fulfillment system that manages Warehouses, Stores, Products, and their relationships. The assignment involves implementing missing features and answering architectural questions.

**Assignment Tasks:** [CODE_ASSIGNMENT](java-assignment/CODE_ASSIGNMENT.md)  
**Questions to Answer:** [QUESTIONS](java-assignment/QUESTIONS.md)  
**Case Study Scenarios:** [CASE_STUDY](case-study/CASE_STUDY.md)

## ✅ Implementation Status

| Task | Description | Status |
|------|-------------|--------|
| Task 1 | LocationGateway.resolveByIdentifier() | ✅ Complete |
| Task 2 | Store Events (after DB commit) | ✅ Complete |
| Task 3 | Warehouse CRUD Operations | ✅ Complete |
| Task 4 | CreateWarehouseUseCase | ✅ Complete |
| Task 5 | ReplaceWarehouseUseCase | ✅ Complete |
| BONUS | Fulfillment Assignments | ✅ Complete |

## 🏗️ Architecture Overview

```
├── location/           # Location resolution service
├── stores/             # Store management with event-driven architecture
├── products/           # Product management
├── warehouses/         # Warehouse domain (hexagonal architecture)
│   ├── adapters/       # REST API & Database adapters
│   └── domain/         # Business logic (use cases, models, ports)
└── fulfillment/        # BONUS: Warehouse-Product-Store assignments
```

### Key Design Patterns

- **Hexagonal Architecture** (Warehouses): Ports & Adapters pattern
- **Event-Driven Architecture** (Stores): CDI Events for post-commit actions
- **Repository Pattern**: Database abstraction layer
- **Domain-Driven Design**: Clear separation of domain models

## 🚀 Quick Start

### Prerequisites

- **Java 21** (required - project uses `maven.compiler.release=21`)
- **Maven 3.8+**
- **Docker** (optional, for PostgreSQL)

### Running the Application

#### Option 1: Using Docker Compose (Recommended)

The easiest way to run the application with PostgreSQL:

```bash
cd java-assignment

# Start PostgreSQL and the application
docker-compose up -d

# View logs
docker-compose logs -f java-assignment

# Stop all services
docker-compose down
```

This will start:
- **PostgreSQL 14** on port 5432
- **Quarkus Application** on port 8080

#### Option 2: Development Mode (with hot reload)

```bash
cd java-assignment

# Start PostgreSQL only
docker-compose up -d postgres

# Run application in dev mode
./mvnw quarkus:dev

# Or with Maven
mvn quarkus:dev
```

#### Option 3: Without Docker

```bash
cd java-assignment

# Run with H2 in-memory database (default for dev)
mvn quarkus:dev
```

The application starts at: http://localhost:8080

### API Documentation (Swagger UI)

Once the application is running, access the interactive API documentation:

| URL | Description |
|-----|-------------|
| http://localhost:8080/swagger-ui | Interactive Swagger UI |
| http://localhost:8080/openapi | OpenAPI specification (YAML) |
| http://localhost:8080/openapi?format=json | OpenAPI specification (JSON) |
| http://localhost:8080/q/dev | Quarkus Dev UI (dev mode only) |

The Swagger UI provides:
- Interactive API exploration
- Request/response examples
- Try-out functionality for all endpoints
- Auto-generated documentation from code annotations

### Running Tests

```bash
cd java-assignment

# Run all tests
mvn clean test

# Run specific test class
mvn test -Dtest=CreateWarehouseUseCaseTest
```

### Building for Production

```bash
cd java-assignment

# Build JAR
mvn clean package

# Build with Docker
docker build -f src/main/docker/Dockerfile.jvm -t fulfillment-app .
```

## 📡 API Endpoints

### Warehouses
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/warehouse` | List all warehouses |
| GET | `/warehouse/{id}` | Get warehouse by ID |
| POST | `/warehouse` | Create warehouse |
| PUT | `/warehouse/{id}` | Replace warehouse |
| DELETE | `/warehouse/{id}` | Archive warehouse |

### Stores
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/store` | List all stores |
| GET | `/store/{id}` | Get store by ID |
| POST | `/store` | Create store |
| PUT | `/store/{id}` | Update store |
| DELETE | `/store/{id}` | Delete store |

### Products
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/products` | List all products |
| GET | `/products/{id}` | Get product by ID |
| POST | `/products` | Create product |
| PUT | `/products/{id}` | Update product |
| DELETE | `/products/{id}` | Delete product |

### Fulfillment Assignments (BONUS)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/fulfillment-assignments` | List all assignments |
| GET | `/fulfillment-assignments/{id}` | Get assignment by ID |
| GET | `/fulfillment-assignments/by-store/{storeId}` | Get by store |
| GET | `/fulfillment-assignments/by-warehouse/{code}` | Get by warehouse |
| GET | `/fulfillment-assignments/by-product/{productId}` | Get by product |
| POST | `/fulfillment-assignments` | Create assignment |
| DELETE | `/fulfillment-assignments/{id}` | Delete assignment |

## 📊 Business Rules

### Warehouse Creation
- Business Unit Code must be unique
- Location must be valid (exists in LocationGateway)
- Cannot exceed max warehouses per location
- Capacity cannot exceed location's max capacity
- Stock cannot exceed capacity

### Warehouse Replacement
- Old warehouse must exist and not be archived
- New warehouse capacity must accommodate existing stock
- Stock from old warehouse is transferred to new
- Old warehouse is archived (soft delete)

### Fulfillment Assignments (BONUS)
- Each Product can be fulfilled by max **2** Warehouses per Store
- Each Store can be fulfilled by max **3** Warehouses
- Each Warehouse can store max **5** product types

## 🧪 Test Coverage

### JaCoCo Code Coverage

The project uses JaCoCo for code coverage tracking with **minimum 80% coverage requirement**.

```bash
# Run tests with coverage
mvn clean verify

# Generate coverage report only
mvn clean test jacoco:report
```

**Coverage Reports Location:**
- HTML Report: `target/site/jacoco/index.html`
- XML Report: `target/site/jacoco/jacoco.xml`
- CSV Report: `target/site/jacoco/jacoco.csv`

**Coverage Requirements:**

| Metric | Minimum |
|--------|---------|
| Line Coverage (Overall) | 80% |
| Branch Coverage (Overall) | 70% |
| Line Coverage (Per Class) | 70% |

### Test Classes

| Test Class | Tests | Coverage Area |
|------------|-------|---------------|
| LocationGatewayTest | 11 | Location resolution |
| LocationModelTest | 9 | Location domain model |
| StoreResourceTransactionTest | 11 | Store events integration |
| StoreResourceTest | 13 | Store REST API |
| StoreEventObserverTest | 8 | Event observer |
| StoreModelTest | 10 | Store entity model |
| LegacyStoreManagerGatewayTest | 10 | Legacy system integration |
| ProductEndpointTest | 1 | Product endpoint |
| ProductResourceTest | 11 | Product REST API |
| ProductRepositoryTest | 14 | Product repository |
| WarehouseRepositoryTest | 18 | Warehouse CRUD |
| WarehouseResourceImplTest | 8 | Warehouse REST API |
| WarehouseModelTest | 7 | Warehouse domain model |
| DbWarehouseTest | 8 | Warehouse entity mapping |
| CreateWarehouseUseCaseTest | 13 | Create validations |
| ReplaceWarehouseUseCaseTest | 9 | Replace validations |
| ArchiveWarehouseUseCaseTest | 9 | Archive functionality |
| FulfillmentAssignmentRepositoryTest | 15 | Assignment CRUD |
| FulfillmentAssignmentResourceTest | 14 | Assignment REST API |
| FulfillmentAssignmentModelTest | 9 | Assignment domain model |
| DbFulfillmentAssignmentTest | 11 | Assignment entity mapping |
| CreateFulfillmentAssignmentUseCaseTest | 15 | Assignment business rules |
| **Total** | **~225 tests** | |
| LocationGatewayTest | 11 | Location resolution |
| StoreResourceTransactionTest | 11 | Store events |
| StoreEventObserverTest | 8 | Event observer |
| WarehouseRepositoryTest | 18 | CRUD operations |
| CreateWarehouseUseCaseTest | 13 | Create validations |
| ReplaceWarehouseUseCaseTest | 9 | Replace validations |
| ArchiveWarehouseUseCaseTest | 9 | Archive functionality |
| FulfillmentAssignmentRepositoryTest | 15 | Assignment CRUD |
| CreateFulfillmentAssignmentUseCaseTest | 10+ | Assignment rules |
| FulfillmentAssignmentResourceTest | 14 | REST API |
| **Total** | **~118 tests** | |

## 🔧 Configuration

### Application Properties
```properties
# Database (H2 for dev, PostgreSQL for prod)
quarkus.datasource.db-kind=postgresql
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/fulfillment

# Hibernate
quarkus.hibernate-orm.database.generation=drop-and-create
quarkus.hibernate-orm.sql-load-script=import.sql
```

### Docker Compose Services

The `docker-compose.yml` includes:

| Service | Image | Port | Description |
|---------|-------|------|-------------|
| postgres | postgres:14-alpine | 5432 | PostgreSQL database |
| java-assignment | (built from Dockerfile) | 8080 | Quarkus application |

```bash
# Start all services
docker-compose up -d

# Start only database
docker-compose up -d postgres

# Rebuild and start
docker-compose up -d --build

# View logs
docker-compose logs -f

# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

**Database Connection (Docker):**
- Host: `localhost` (from host) or `postgres` (from container)
- Port: `5432`
- Database: `quarkus`
- Username: `quarkus`
- Password: `quarkus`

## 📁 Project Structure

```
fcs-interview-code-assignment-main/
├── README.md                    # This file
├── api-collections/             # API testing collections
│   ├── postman/                 # Postman collection
│   │   └── Fulfilment_Cost_Control_API.postman_collection.json
│   └── bruno/                   # Bruno collection
│       ├── bruno.json
│       ├── environments/
│       ├── warehouses/
│       ├── stores/
│       ├── products/
│       └── fulfillment-assignments/
├── case-study/
│   ├── BRIEFING.md             # Domain overview
│   └── CASE_STUDY.md           # Discussion scenarios (answered)
└── java-assignment/
    ├── CODE_ASSIGNMENT.md       # Task descriptions
    ├── QUESTIONS.md             # Architectural questions (answered)
    ├── docker-compose.yml       # Docker services
    ├── pom.xml                  # Maven configuration
    └── src/
        ├── main/java/com/fulfilment/application/monolith/
        │   ├── location/        # LocationGateway
        │   ├── stores/          # Store + Events
        │   ├── products/        # Product CRUD
        │   ├── warehouses/      # Warehouse domain
        │   └── fulfillment/     # BONUS: Assignments
        └── test/java/           # Unit & Integration tests
```

## 📬 API Collections

Pre-configured API collections are available for testing:

### Postman

Import the collection into Postman:

1. Open Postman
2. Click **Import** → **Upload Files**
3. Select `api-collections/postman/Fulfilment_Cost_Control_API.postman_collection.json`
4. The collection includes a `baseUrl` variable set to `http://localhost:8080`

### Bruno

[Bruno](https://www.usebruno.com/) is a fast, Git-friendly API client. To use:

1. Open Bruno
2. Click **Open Collection**
3. Navigate to `api-collections/bruno/`
4. Select the folder to open the collection

**Collection Structure:**
```
bruno/
├── bruno.json              # Collection config
├── environments/
│   └── local.bru          # Local environment (baseUrl)
├── warehouses/            # Warehouse endpoints
├── stores/                # Store endpoints
├── products/              # Product endpoints
└── fulfillment-assignments/  # Assignment endpoints
```

### Available Requests

| Category | Requests |
|----------|----------|
| Warehouses | List, Get, Create, Replace, Archive |
| Stores | List, Get, Create, Update, Delete |
| Products | List, Get, Create, Update, Delete |
| Fulfillment Assignments | List, Get by ID/Store/Warehouse/Product, Create, Delete |

## 🔄 Production Readiness Notes

### Current Implementation
- Uses CDI Events for in-process event handling (Task 2)
- In-memory location registry (Task 1)
- H2 database for development

### For Production Scale
- Consider Kafka/RabbitMQ for distributed events
- Persistent location storage (database-backed)
- PostgreSQL for production database
- Add caching layer (Redis)
- Implement circuit breakers for external integrations

## 📚 References

- [Quarkus Documentation](https://quarkus.io/guides/)
- [Quarkus Quickstarts](https://github.com/quarkusio/quarkus-quickstarts)
- [Hibernate ORM with Panache](https://quarkus.io/guides/hibernate-orm-panache)
- [CDI Events](https://quarkus.io/guides/cdi-reference)

## 👤 Author

Completed as part of the Fulfilment Cost Control System interview assignment.

---

*Last updated: March 2, 2026*
