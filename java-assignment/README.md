# Java Code Assignment

This is a Quarkus-based Fulfillment Cost Control System implementing Hexagonal Architecture, Event-Driven patterns, and RESTful APIs for Warehouse, Store, and Product management.

## ✅ Assignment Completion Status

| Requirement | Status |
|-------------|--------|
| All tasks from CODE_ASSIGNMENT.md | ✅ Complete |
| Unit tests (positive, negative, error conditions) | ✅ Complete |
| JaCoCo code coverage (80%+) | ✅ Configured |
| Code quality & standards | ✅ Followed |
| Exception handling & logging | ✅ Implemented |
| Case study documentation | ✅ Documented |
| Docker support | ✅ Included |
| API documentation (Swagger) | ✅ Included |

## About the Assignment

Tasks completed from [CODE_ASSIGNMENT](CODE_ASSIGNMENT.md):
- **Task 1:** LocationGateway.resolveByIdentifier() implementation
- **Task 2:** Store Events (CDI Events fired after DB commit)
- **Task 3:** Warehouse CRUD Operations
- **Task 4:** CreateWarehouseUseCase with business rules
- **Task 5:** ReplaceWarehouseUseCase implementation
- **BONUS:** Fulfillment Assignments feature

## Requirements

- **JDK 21** (project uses Java 21 features)
- **Maven 3.8+**
- **Docker** (optional, for PostgreSQL)

### Configuring JDK 21

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
```

## Quick Start

### Option 1: Docker Compose (Recommended)

```bash
# Start PostgreSQL and application
docker-compose up -d

# View logs
docker-compose logs -f java-assignment

# Stop
docker-compose down
```

### Option 2: Development Mode

```bash
# Run with hot reload
./mvnw quarkus:dev
```

### Option 3: Build and Run

```bash
# Build
./mvnw package

# Run PostgreSQL
docker run -it --rm=true --name quarkus_test \
  -e POSTGRES_USER=quarkus_test \
  -e POSTGRES_PASSWORD=quarkus_test \
  -e POSTGRES_DB=quarkus_test \
  -p 15432:5432 postgres:14-alpine

# Run application
java -jar ./target/quarkus-app/quarkus-run.jar
```

## Running Tests

```bash
# Run all tests
./mvnw clean test

# Run tests with coverage report
./mvnw clean verify

# View coverage report
open target/site/jacoco/index.html
```

## API Documentation

Once running, access:
- **Swagger UI:** http://localhost:8080/swagger-ui
- **OpenAPI spec:** http://localhost:8080/openapi

## Test Coverage

JaCoCo is configured with **80% minimum line coverage**. Coverage report is generated at `target/site/jacoco/index.html`.

| Test Class | Tests |
|------------|-------|
| LocationGatewayTest | 11 |
| StoreResourceTransactionTest | 11 |
| StoreResourceTest | 13 |
| StoreEventObserverTest | 8 |
| ProductResourceTest | 11 |
| ProductRepositoryTest | 14 |
| WarehouseRepositoryTest | 18 |
| CreateWarehouseUseCaseTest | 13 |
| ReplaceWarehouseUseCaseTest | 9 |
| ArchiveWarehouseUseCaseTest | 9 |
| FulfillmentAssignmentResourceTest | 14 |
| FulfillmentAssignmentRepositoryTest | 15 |
| + Model & Entity Tests | 50+ |
| **Total** | **~200+ tests** |

## Project Structure

```
src/
├── main/java/com/fulfilment/application/monolith/
│   ├── location/        # Location resolution (Task 1)
│   ├── stores/          # Store + Events (Task 2)
│   ├── products/        # Product management
│   ├── warehouses/      # Hexagonal architecture (Tasks 3-5)
│   │   ├── adapters/    # REST API & Database
│   │   └── domain/      # Business logic
│   └── fulfillment/     # BONUS: Assignments
└── test/java/           # Comprehensive unit tests
```

## Code Quality

- **Architecture:** Hexagonal (Ports & Adapters)
- **Design Patterns:** Repository, Use Case, Event-Driven
- **Testing:** JUnit 5, REST Assured, Quarkus Test
- **Coverage:** JaCoCo (80%+ required)
- **API Docs:** OpenAPI/Swagger

## Troubleshooting

**IntelliJ:** Add `target/generated-sources/jaxrs` as generated sources if compilation fails.

**Java Version:** Ensure Java 21 is being used:
```bash
java -version  # Should show 21.x
```

---

Based on https://github.com/quarkusio/quarkus-quickstarts
