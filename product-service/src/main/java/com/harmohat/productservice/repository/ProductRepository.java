package com.harmohat.productservice.repository;

import java.util.List;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.harmohat.productservice.model.Product;

@Repository
public class ProductRepository {
	
	private final Map<Integer, Product> products = new HashMap<>();
	private int nextId = 1;
	
	public ProductRepository() {
		this.save("Laptop", new BigDecimal("1200.00"));
		this.save("Keyboard", new BigDecimal("50.00"));
		this.save("Mouse", new BigDecimal("25.00"));
	}
	
	public List<Product> findAll() {
		return new ArrayList<>(products.values());
	}
	
	public Optional<Product> findById(int id) {
		return Optional.ofNullable(products.get(id));
	}
	
	public Product save(String name, BigDecimal price) {
		int id = nextId++;
		Product product = new Product(id, name, price);
		products.put(id, product);
		return product;
	}
}
