package com.example.marketplace.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "marketplace_trade_message")
public class TradeMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trade_request_id", nullable = false)
    private Long tradeRequestId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTradeRequestId() {
        return tradeRequestId;
    }

    public void setTradeRequestId(Long tradeRequestId) {
        this.tradeRequestId = tradeRequestId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


