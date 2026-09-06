package com.harmohat.productservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.harmohat.productservice.exception.ProductNotFoundException;
import com.harmohat.productservice.model.Product;
import com.harmohat.productservice.service.ProductService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

	@Autowired
	private MockMvc mockMvc;
	
	@MockitoBean
	private ProductService productService;
	
	@Test
	void getAllReturns200WithProducts() throws Exception {
        // Service returns one product
		when(productService.getAll()).thenReturn(List.of(
            new Product(1, "Laptop", new BigDecimal("1200.00"))
		));
		
        // GET /products returns 200 with the product list
		mockMvc.perform(get("/products"))
				.andExpect(status().isOk())
			    .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Laptop"));
	}
	
	@Test
    void getByIdReturns200WhenFound() throws Exception {
        // Service returns the product for id 1
        when(productService.getById(1)).thenReturn(new Product(1, "Laptop", new BigDecimal("1200.00")));

        // GET /products/1 returns 200 with the product
        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }
	
    @Test
    void getByIdReturns404WhenNotFound() throws Exception {
        // Service throws when the product does not exist
        when(productService.getById(999))
                .thenThrow(new ProductNotFoundException("Product not found for 999"));

        // GET /products/999 returns 404
        mockMvc.perform(get("/products/999"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void createReturns201WhenValid() throws Exception {
        // Service creates and returns the new product
        when(productService.create(eq("Webcam"), any(BigDecimal.class)))
                .thenReturn(new Product(4, "Webcam", new BigDecimal("79.99")));

        String json = "{\"name\": \"Webcam\", \"price\": 79.99}";

        // POST /products with valid data returns 201 with the created product
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.name").value("Webcam"));
    }

    @Test
    void createReturns400WhenInvalid() throws Exception {
        // Invalid data: name too short and negative price
        String invalidJson = "{\"name\": \"ab\", \"price\": -5}";

        // POST /products with invalid data returns 400 (validation fails)
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}