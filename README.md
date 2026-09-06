# Product & Inventory Microservices

Two lightweight Spring Boot microservices for an e-commerce scenario:

- **Product Service** manages products (create, list, get by id) and exposes a combined `details` endpoint that also returns stock information.
- **Inventory Service** stores stock levels and exposes a lookup endpoint.

The Product Service calls the Inventory Service over HTTP to build the combined product details response.

## Tech stack

- Java 25
- Spring Boot 4.1.1 (Spring Web, Spring Validation)
- Maven
- JUnit 5, Mockito, MockMvc for testing
- In-memory storage (no database required)

## Prerequisites

- Java 21 or later (Java 25 recommended)
- Maven 3.9+ (a Maven wrapper `mvnw` is included, so a local Maven install is
  optional)

## Project structure

```
abn-assignment/
├── inventory-service/     # stock lookup service (port 8081)
├── product-service/       # product service + details endpoint (port 8080)
├── NOTES.md               # design choices and reasoning
└── README.md
```

Each service is a standalone Maven project with its own `pom.xml`.

## Ports

| Service           | Port |
|-------------------|------|
| Product Service   | 8080 |
| Inventory Service | 8081 |

The Product Service reads the Inventory Service URL from `application.properties` (`inventory.service.url=http://localhost:8081`).

## How to build

Build each service from its own folder:

```
cd inventory-service
mvn clean verify

cd ../product-service
mvn clean verify
```

`mvn clean verify` compiles the code and runs the tests. 

## How to run

Both services must be running to use the `/products/{id}/details` endpoint.
Start the Inventory Service first, then the Product Service.

```
cd inventory-service
mvn spring-boot:run

cd ../product-service
mvn spring-boot:run
```

The Inventory Service starts on port 8081, the Product Service on port 8080.

## Test data

Both services load a small in-memory dataset on startup, with matching ids so
the services line up:

| id | Product  | Price   | Stock |
|----|----------|---------|-------|
| 1  | Laptop   | 1200.00 | 20    |
| 2  | Keyboard | 50.00   | 15    |
| 3  | Mouse    | 25.00   | 10    |

## API examples

### Inventory Service (port 8081)

Get inventory for a product:

```
curl http://localhost:8081/inventory/1
```

```json
{ "productId": 1, "quantity": 20 }
```

Unknown product returns 404.

### Product Service (port 8080)

Get all products:

```
curl http://localhost:8080/products
```

Get one product:

```
curl http://localhost:8080/products/1
```

Create a product (returns 201 Created):

```
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d "{\"name\": \"Webcam\", \"price\": 79.99}"
```

Invalid create request returns 400 with the validation errors:

```
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d "{\"name\": \"ab\", \"price\": -5}"
```

```json
{
  "message": "Validation failed",
  "errors": [
    "name must be at least 3 characters",
    "price must be greater than 0"
  ]
}
```

Get combined product details (product + stock). This calls the Inventory Service:

```
curl http://localhost:8080/products/1/details
```

```json
{ "id": 1, "name": "Laptop", "price": 1200.00, "quantity": 20 }
```

## Status codes

| Situation                             | Status |
|---------------------------------------|--------|
| Success                               | 200    |
| Product created                       | 201    |
| Invalid product input (validation)    | 400    |
| Product or inventory not found        | 404    |
| Inventory Service unreachable         | 503    |

## Testing and coverage

Run the tests per service with `mvn clean verify` (or via your IDE). Coverage
can be viewed in the IDE (e.g. Eclipse: Coverage As > JUnit Test). Line
coverage on the production code is around 89% for the Inventory Service and
85% for the Product Service, above the 70% target.

Tests cover the service logic (with mocked dependencies) and the controllers
(with MockMvc), including the not-found, validation, and service-communication
cases.

## Assumptions and choices

- **In-memory storage, no database**, as required. Data resets on restart.
- **Matching ids** are used across both services so the `details` endpoint
  lines up. A product created via `POST /products` has no inventory record,
  so `details` for it returns 404 (no inventory), which is handled explicitly.
- **The Inventory Service is independent** and does not check the Product
  Service; each service owns its own data.
- **`price` uses BigDecimal** rather than double, to avoid rounding errors
  with money.
- **The `details` endpoint distinguishes** a missing inventory record (404)
  from the Inventory Service being unreachable (503).
- Further design reasoning is documented in `NOTES.md`.