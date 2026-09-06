# Choices and reasoning

## The assignment
- 2 microservices: There is an Inventory (simple lookup) and Product (CRUD + a combined `/details` endpoint that calls the Inventory).
	- Inventory service: This service will store the stock in-memory with one lookup endpoint. It only knows its own data. It does not check the product service.
	- Product service: manages the products (GET all, GET by id, POST create) and has an endpoint `GET /products/{id}/details` that combines its own product data with the stock from the Inventory service
	
### Key points I understand
- Both services store in-memory (no database). I insert the product and inventory test data myself, using matching ids so they lineup.
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
### General (applies to both services)
- Constructor injection everywhere (not field injection with @Autowired):
  makes dependencies explicit, allows final fields, and is easier to test.
- Custom exceptions extend RuntimeException (unchecked), so no throws is
  needed; they go up to the central handler.
- Error responses use a simple Map (message + timestamp / errors). A
  dedicated ErrorResponse class would be cleaner in a bigger project, but
  would be over engineering here.
 
### Inventory Service
- Inventory model: productId (int) and quantity (int).
  The ids and quantities are small whole numbers, so int is enough. In a real
  system with very large ids I might use long, but that is not needed here.
- InventoryRepository: I chose a Map because lookups are always by productId, 
  and a Map gives that in O(1) instead of looping through a List (O(n)). It 
  stores the data in-memory in a Map<Integer, Inventory>, keyed by productId. 
  findByProductId returns Optional<Inventory> instead  of null, to make "not found" 
  explicit and avoid NullPointerExceptions.
- InventoryService: getByProductId uses Optional.orElseThrow (not an if/else null check) 
  to keep it to one line and make the not found path explicit. It
  throws InventoryNotFoundException so the case is handled centrally as a 404, 
  and includes the productId so the error is specific.
- InventoryController: exposes `GET /inventory/{productId}`. I kept it simple, 
  it only receives the request and calls the service. 
- Exception handling: centralized with @RestControllerAdvice so error handling is in 
  one place instead of repeated in every controller. InventoryNotFoundException maps to 404, 
  since "not found" is not a server error (500). I was new to @RestControllerAdvice, so I 
  used the Spring docs: docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-advice.html

### Product Service
- Product model: id (int), name (String), price (BigDecimal).
  I chose BigDecimal over double for the price. double can cause rounding
  errors with money. BigDecimal uses more memory and is
  slower, but for money correctness matters more than that small cost.
- ProductRequest DTO: `POST /products` uses a ProductRequest DTO (name + price, no id) 
  instead of the Product model. The client shouldn't set the id. Validation (@NotBlank, @Size min 3, @DecimalMin) 
  lives on the DTO, so invalid input returns 400 automatically via @Valid. 
- ProductRepository: It stores the data in-memory in a Map<Integer, Product>. The client 
  sends no id on create, so the repository assigns one with a nextId counter (a real database 
  would auto-increment). findById returns Optional, like Inventory.
- ProductService: getAll, getById (orElseThrow ProductNotFoundException), and create. 
  Same pattern as the Inventory Service.
- ProductController: exposes `GET /products`, `GET /products/{id}`, 
  and `POST /products`. POST returns 201 Created (not 200) and uses @Valid on the DTO. 
  The other endpoints return the data directly.
- Exception handling: @RestControllerAdvice handles two cases. ProductNotFoundException 
  maps to 404, and validation failures (MethodArgumentNotValidException) map to 
  400 with a list of the field error messages.
  - The /details endpoint is the only place the two services meet. The Product
  service calls Inventory over HTTP, Inventory stays unaware of Product, so
  they stay loosely coupled and can run independently.
- RestTemplate over WebClient: the call is a simple synchronous request, so RestTemplate 
  is enough. WebClient would add reactive complexity I don't need here.
- I used RestTemplate as a @Bean instead of `new RestTemplate()` per call: one 
  reused instance is cleaner (DRY) than creating a new one each time.
  - I put the RestTemplate bean in its own config package ([online research](https://zetcode.com/springboot/resttemplate/)). 
  Not in the suggested structure, but the assignment allows alternatives when well organised, 
  and separating config is cleaner than putting it in the main class.
- I split the `/details` errors into 404 (no inventory record) and 503 (service unreachable): 
  different causes deserve different status codes, so the user knows whether the data 
  is missing or the service is down.
- Two DTOs for the communication: InventoryResponse receives the inventory JSON 
  (needs a no args constructor and setters, since  Jackson fills it), and ProductDetailsResponse 
  is the outgoing combined answer (constructor + getters, since Jackson reads it). Keeping them 
  separate from the Product model avoids exposing the internal model over the API.
- Inventory URL via @Value in the constructor (constructor injection), not on the field:
  this keeps it consistent with the other dependencies.
  
## Testing
### Inventory Service
- I made tests for the service and controller, since those hold the logic worth verifying (found vs not found, and mapping to the right HTTP status). I mocked the dependency in each test, so the service and controller are tested in isolation.

### Product Service
- Same two level approach as Inventory (service tests + controller tests), because testing both the logic and the web layer gives fuller coverage than either alone.
- Tested the meaningful paths (found/not found, validation, the three
 /details cases).
- Extracted the inventory URL into a constant (INVENTORY_URL) to avoid a  magic value.

