package com.harmohat.productservice.dto;

import java.math.BigDecimal;

public class ProductDetailsResponse {

	private int id;
	private String name;
	private BigDecimal price;
	private int quantity;
	
	public ProductDetailsResponse(int id, String name, BigDecimal price, int quantity) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.quantity = quantity;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public int getQuantity() {
		return quantity;
	}
}
