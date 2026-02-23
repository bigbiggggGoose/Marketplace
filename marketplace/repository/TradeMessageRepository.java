package com.example.marketplace.repository;

import com.example.marketplace.model.TradeMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TradeMessageRepository extends JpaRepository<TradeMessage, Long> {
    List<TradeMessage> findByTradeRequestIdOrderByCreatedAtAsc(Long tradeRequestId);
    void deleteByTradeRequestIdIn(List<Long> tradeRequestIds);
}


