package com.example.marketplace.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "marketplace_trade_request")
public class TradeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "seller_offer_price", precision = 10, scale = 2)
    private java.math.BigDecimal sellerOfferPrice;

    @Column(name = "seller_offer_quantity")
    private Integer sellerOfferQuantity;

    @Column(name = "buyer_confirmed", nullable = false)
    private Boolean buyerConfirmed = false;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "PENDING"; // PENDING, ACCEPTED, REJECTED, SELLER_OFFERED, BUYER_CONFIRMED, COMPLETED, CANCELLED

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
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

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
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

