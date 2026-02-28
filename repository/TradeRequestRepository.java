package com.example.marketplace.repository;

import com.example.marketplace.model.TradeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TradeRequestRepository extends JpaRepository<TradeRequest, Long> {
    List<TradeRequest> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);
    List<TradeRequest> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
    List<TradeRequest> findByItemId(Long itemId);
    List<TradeRequest> findByItemIdAndBuyerId(Long itemId, Long buyerId);
    void deleteByItemId(Long itemId);
}

