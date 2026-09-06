package com.harmohat.productservice.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.harmohat.productservice.exception.ProductNotFoundException;
import com.harmohat.productservice.model.Product;
import com.harmohat.productservice.repository.ProductRepository;

@Service
public class ProductService {

	private final ProductRepository productRepository;
	
	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
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
}
