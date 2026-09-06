package com.harmohat.inventoryservice.model;

public class Inventory {
	private int productId;
    private int quantity;

    public Inventory(int productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public int getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
}
