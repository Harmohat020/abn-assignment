# Choices and reasoning

## The assignment
- 2 microservices: There is an Inventory (simple lookup) and Product (CRUD + a combined `/details` endpoint that calls the Inventory).
	- Inventory service: This service will store the stock in-memory with one lookup endpoint. It only knows it own data. It does not check the product service.
	- Product service: manages the products (GET all, GET by id, POST create) and has an endpoint `GET /products/{id}/details` that combines its own product data with the stock from the Inventory service
	
### Key points I understand
- Both services store in-memory (no database). I insert the product and inventory test data myself, using mathcing ids so they lineup.
- The link between the two is the productId.
- The Inventory service does not verify products with the Product service. Each service stays independent.
- A product created via `POST /products` will have no inventory record, because there is no way to add inventory (No POST on Inventory). I will handle this as a "missing inventory" case in `/details`.
	
## Approach
1. I choose to build Inventory first. Thinking about it, Product comes first (no product means no inventory), from a technical perspective the Product Service will call the Inventory Service for the `/details` endpoint. So building the Inventory first means it's ready and testable when I implement that combined endpoint. Inventory is also simpler, which makes it a good start point.
2. Then the Product service
3. Then the service communication: the `/details` endpoint that calls Inventory.

## Layers
- Controller
- Service
- Repository
- Model/DTO
- Exception handling

## Project setup
- In the document there is a suggested project structure, and because we need two Spring Boot microservices I placed the projects in one git repo (monorepo).

## Implementation choices
- Inventory model: productId (int) and quantity (int).
The ids and quantities are small whole number, so int is enough. In a real system with very large ids I might use long, but that is not needed here.
- InventoryRepository: It stores the data in-memory in a Map<Integer, Inventory>, keyed by productId. findByProductId returns Optional<Inventory> instead  of null, to make "not found" explicit and avoid NullPointerExceptions.
- InventoryService: getByProductId uses Optional.orElseThrow to return the inventory or throw InventoryNotFoundException with a clear message including the productId.
- InventoryController: exposes `GET /inventory/{productId}`. I kept it simple, it only receives the request and calls the service. 
- Exception handling: centralized with @RestControllerAdvice so the error handling is in one place. InventoryNotFoundException maps to 404, because "not found" is not a server error (500). I was not familiar with @RestControllerAdvice, so I used the Spring docs: docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-advice.html

