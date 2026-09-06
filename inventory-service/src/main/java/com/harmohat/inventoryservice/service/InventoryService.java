package com.harmohat.inventoryservice.service;

import org.springframework.stereotype.Service;

import com.harmohat.inventoryservice.exception.InventoryNotFoundException;
import com.harmohat.inventoryservice.model.Inventory;
import com.harmohat.inventoryservice.repository.InventoryRepository;

@Service
public class InventoryService {
	
	private final InventoryRepository inventoryRepository;
	
	public InventoryService(InventoryRepository inventoryRepository) {
		this.inventoryRepository = inventoryRepository;
	}
	
	public Inventory getByProductId(int productId) {
		return inventoryRepository.findByProductId(productId)
				.orElseThrow(() -> new InventoryNotFoundException("Inventory not found for product " + productId));
		
	}
}
