package com.harmohat.inventoryservice.repository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.harmohat.inventoryservice.model.Inventory;

@Repository
public class InventoryRepository {
	
	private final Map<Integer, Inventory> inventoryData = new HashMap<>();
	
	public InventoryRepository() {
		inventoryData.put(1, new Inventory(1, 20));
		inventoryData.put(2, new Inventory(2, 15));
		inventoryData.put(3, new Inventory(3, 10));
	}
	
	public Optional<Inventory> findByProductId(int productId) {
		return Optional.ofNullable(inventoryData.get(productId));
	}
}
