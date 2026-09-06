package com.harmohat.productservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.harmohat.productservice.dto.InventoryResponse;
import com.harmohat.productservice.dto.ProductDetailsResponse;
import com.harmohat.productservice.exception.InventoryNotFoundException;
import com.harmohat.productservice.exception.InventoryUnavailableException;
import com.harmohat.productservice.exception.ProductNotFoundException;
import com.harmohat.productservice.model.Product;
import com.harmohat.productservice.repository.ProductRepository;

public class ProductServiceTest {
	
    private static final String INVENTORY_URL = "http://localhost:8081";

    // Helper method to build the service with mocked dependencies
	private ProductService buildService(ProductRepository repository, RestTemplate restTemplate) {
		return new ProductService(repository, restTemplate, INVENTORY_URL);
	}
	
	@Test
	void getAllReturnsAllProducts() {
		// Repository returns one product
		ProductRepository repository = mock(ProductRepository.class);
		RestTemplate restTemplate = mock(RestTemplate.class);
		
		when(repository.findAll()).thenReturn(List.of(
				new Product(1, "Laptop", new BigDecimal("1200.00"))
		));
		
		ProductService service = buildService(repository, restTemplate);
		
		// Get all products
		List<Product> result = service.getAll();
		
		// The list contains the product
		assertEquals(1, result.size());
		assertEquals("Laptop", result.get(0).getName());
	}
	
	@Test
	void getByIdReturnsProductWhenFound() {
        // Repository returns the product for id 1
		ProductRepository repository = mock(ProductRepository.class);
		RestTemplate restTemplate = mock(RestTemplate.class);
		
		when(repository.findById(1)).thenReturn(Optional.of(new Product(1, "Laptop", new BigDecimal("1200.00"))));
		
		ProductService service = buildService(repository, restTemplate);
		
        // Get the product by id
		Product resultProduct = service.getById(1);
		
        // The correct product is returned
		assertEquals(1, resultProduct.getId());
		assertEquals("Laptop", resultProduct.getName());
	}
	
	@Test
	void getByIdThrowsWhenNotFound() {
        // Repository returns empty for id 999
		ProductRepository repository = mock(ProductRepository.class);
		RestTemplate restTemplate = mock(RestTemplate.class);
		
		when(repository.findById(999)).thenReturn(Optional.empty());
		
		ProductService service = buildService(repository, restTemplate);
		
        // Getting a non existing product throws ProductNotFoundException
		assertThrows(ProductNotFoundException.class, () -> service.getById(999));
	}
	
	@Test
	void createSavesAndReturnsProduct() {
        // Repository saves and returns the new product
		ProductRepository repository = mock(ProductRepository.class);
		RestTemplate restTemplate = mock(RestTemplate.class);
		Product saved = new Product(4, "Webcam", new BigDecimal("79.99"));
       
		when(repository.save("Webcam", new BigDecimal("79.99"))).thenReturn(saved);
	
		ProductService service = buildService(repository, restTemplate);
		
        // Create the product
		Product result = service.create("Webcam", new BigDecimal("79.99"));
		
        // The saved product is returned with its id
		assertEquals(4, result.getId());
		assertEquals("Webcam", result.getName());
	}
	
	
    @Test
    void getDetailsReturnsCombinedResponse() {
        // Product exists and inventory returns a quantity
        ProductRepository repository = mock(ProductRepository.class);
        RestTemplate restTemplate = mock(RestTemplate.class);

        when(repository.findById(1)).thenReturn(Optional.of(new Product(1, "Laptop", new BigDecimal("1200.00"))));

        InventoryResponse inventory = new InventoryResponse();
        inventory.setProductId(1);
        inventory.setQuantity(20);
        when(restTemplate.getForObject(INVENTORY_URL + "/inventory/1", InventoryResponse.class))
                .thenReturn(inventory);

        ProductService service = buildService(repository, restTemplate);

        // Get the product details
        ProductDetailsResponse result = service.getDetails(1);

        // Product data and quantity are combined
        assertEquals(1, result.getId());
        assertEquals("Laptop", result.getName());
        assertEquals(20, result.getQuantity());
    }

    @Test
    void getDetailsThrowsWhenInventoryNotFound() {
        // Product exists, but the inventory service returns 404
        ProductRepository repository = mock(ProductRepository.class);
        RestTemplate restTemplate = mock(RestTemplate.class);

        when(repository.findById(4)).thenReturn(Optional.of(new Product(4, "Monitor", new BigDecimal("199.99"))));
        when(restTemplate.getForObject(INVENTORY_URL + "/inventory/4", InventoryResponse.class))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        ProductService service = buildService(repository, restTemplate);

        // This maps to an InventoryNotFoundException
        assertThrows(InventoryNotFoundException.class, () -> service.getDetails(4));
    }

    @Test
    void getDetailsThrowsWhenInventoryUnavailable() {
        // Product exists, but the inventory service is unreachable
        ProductRepository repository = mock(ProductRepository.class);
        RestTemplate restTemplate = mock(RestTemplate.class);

        when(repository.findById(1)).thenReturn(Optional.of(new Product(1, "Laptop", new BigDecimal("1200.00"))));
        when(restTemplate.getForObject(INVENTORY_URL + "/inventory/1", InventoryResponse.class))
                .thenThrow(new ResourceAccessException("Connection refused"));

        ProductService service = buildService(repository, restTemplate);

        // This maps to an InventoryUnavailableException
        assertThrows(InventoryUnavailableException.class, () -> service.getDetails(1));
    }
}
