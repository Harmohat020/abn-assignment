package com.harmohat.inventoryservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.harmohat.inventoryservice.model.Inventory;
import com.harmohat.inventoryservice.service.InventoryService;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
	
	private final InventoryService inventoryService;
	
	public InventoryController(InventoryService inventoryService) {
		this.inventoryService = inventoryService;
	}
	
	@GetMapping("/{productId}") 
	public Inventory getByProductId(@PathVariable int productId) {
		return inventoryService.getByProductId(productId);
	}
}
