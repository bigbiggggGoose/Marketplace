package com.example.marketplace.dto;

public class TradeRequestCreateRequest {
    private String message;
    private Integer quantity;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}

