package com.harmohat.inventoryservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.harmohat.inventoryservice.exception.InventoryNotFoundException;
import com.harmohat.inventoryservice.model.Inventory;
import com.harmohat.inventoryservice.repository.InventoryRepository;

class InventoryServiceTest {

	@Test
	void returnsInventoryWhenItExists() {
        // mock repository that returns inventory for product 1
		InventoryRepository repository = mock(InventoryRepository.class);
		when(repository.findByProductId(1)).thenReturn(Optional.of(new Inventory(1, 20)));
		
		InventoryService service = new InventoryService(repository);
		
		// look up the inventory for product 1
		Inventory result = service.getByProductId(1);
		
		// got the correct data back
		assertEquals(1, result.getProductId());
		assertEquals(20, result.getQuantity());
	}
	
	@Test
	void throwsExceptionWhenInventoryDoesNotExist() {
		// mock repository that returns nothing
		InventoryRepository repository = mock(InventoryRepository.class);
        when(repository.findByProductId(999)).thenReturn(Optional.empty());
        
        InventoryService service = new InventoryService(repository);
        
        // expect an exception to be thrown 
        assertThrows(InventoryNotFoundException.class, () -> service.getByProductId(999));
	}
}
