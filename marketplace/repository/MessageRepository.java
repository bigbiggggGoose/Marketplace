package com.example.marketplace.repository;

import com.example.marketplace.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByItemIdOrderByCreatedAtAsc(Long itemId);
    List<Message> findBySenderIdOrReceiverIdOrderByCreatedAtDesc(Long senderId, Long receiverId);
    void deleteByItemId(Long itemId);
}

