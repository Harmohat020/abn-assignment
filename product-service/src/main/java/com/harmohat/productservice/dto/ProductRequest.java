package com.harmohat.productservice.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProductRequest {
	
	@NotBlank(message = "name must not be blank")
	@Size(min = 3, message = "name must be at least 3 characters")
	private String name;
	
	@NotNull(message = "price is required")
	@DecimalMin(value = "0.01", message = "price must be greater than 0")
	private BigDecimal price;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}
}
