package com.harmohat.productservice.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

@Service
public class ProductService {

	private final ProductRepository productRepository;
	private final RestTemplate restTemplate;
	private final String inventoryServiceUrl;
	
	public ProductService(ProductRepository productRepository, 
						  RestTemplate restTemplate,  
						  @Value("${inventory.service.url}") String inventoryServiceUrl) {
		this.productRepository = productRepository;
		this.restTemplate = restTemplate;
		this.inventoryServiceUrl = inventoryServiceUrl;
		
	}
	
	public List<Product> getAll() {
		return productRepository.findAll();
	}
	
	public Product getById(int id) {
		return productRepository.findById(id)
				.orElseThrow(() -> new ProductNotFoundException("Product not found for " + id));
		
	}
	
	public Product create(String name, BigDecimal price) {
		return productRepository.save(name, price);
	}
	
	public ProductDetailsResponse getDetails(int id) {
		Product product = getById(id);
		
		String url = inventoryServiceUrl + "/inventory/" + id;
		InventoryResponse inventory;
		
		try {
			inventory = restTemplate.getForObject(url, InventoryResponse.class);
		} catch (HttpClientErrorException.NotFound e) {
	        throw new InventoryNotFoundException("No inventory found for product " + id);
		} catch (ResourceAccessException ex) {
	        throw new InventoryUnavailableException("Inventory Service is unavailable");
	    }
	
		int quantity = inventory.getQuantity();
		
		return new ProductDetailsResponse(
			product.getId(),
			product.getName(),
			product.getPrice(),
			quantity
		);	
	}
}
