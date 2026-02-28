package com.example.marketplace.dto;

import java.time.LocalDateTime;

public class TradeRequestDto {
    private Long id;
    private Long itemId;
    private String itemTitle;
    private Long buyerId;
    private String buyerName;
    private Long sellerId;
    private String sellerName;
    private String message;
    private Integer quantity;
    private java.math.BigDecimal sellerOfferPrice;
    private Integer sellerOfferQuantity;
    private Boolean buyerConfirmed;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public java.math.BigDecimal getSellerOfferPrice() {
        return sellerOfferPrice;
    }

    public void setSellerOfferPrice(java.math.BigDecimal sellerOfferPrice) {
        this.sellerOfferPrice = sellerOfferPrice;
    }

    public Integer getSellerOfferQuantity() {
        return sellerOfferQuantity;
    }

    public void setSellerOfferQuantity(Integer sellerOfferQuantity) {
        this.sellerOfferQuantity = sellerOfferQuantity;
    }

    public Boolean getBuyerConfirmed() {
        return buyerConfirmed;
    }

    public void setBuyerConfirmed(Boolean buyerConfirmed) {
        this.buyerConfirmed = buyerConfirmed;
    }
}

