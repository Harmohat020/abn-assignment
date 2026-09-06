package com.harmohat.inventoryservice.controller;

import com.harmohat.inventoryservice.exception.InventoryNotFoundException;
import com.harmohat.inventoryservice.model.Inventory;
import com.harmohat.inventoryservice.service.InventoryService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockitoBean
	private InventoryService inventoryService;
	
	@Test
	void returns200AndInventoryWhenFound() throws Exception {
		// the service returns inventory for product 1
		when(inventoryService.getByProductId(1)).thenReturn(new Inventory(1, 20));
		
		// GET /inventory/1 returns 200 with the correct JSON
		mockMvc.perform(get("/inventory/1"))
				.andExpect(status().isOk())
			    .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.quantity").value(20));
	}
	
	 @Test
    void returns404WhenInventoryNotFound() throws Exception {
        // the service throws when inventory does not exist
        when(inventoryService.getByProductId(999))
                .thenThrow(new InventoryNotFoundException("Inventory not found for product 999"));

        // GET /inventory/999 returns 404
        mockMvc.perform(get("/inventory/999"))
                .andExpect(status().isNotFound());
    }
}
