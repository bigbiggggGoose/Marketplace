package com.example.marketplace.service;

import com.example.marketplace.dto.MessageDto;
import com.example.marketplace.dto.MessageRequest;
import com.example.marketplace.model.Item;
import com.example.marketplace.model.Message;
import com.example.marketplace.model.User;
import com.example.marketplace.repository.ItemRepository;
import com.example.marketplace.repository.MessageRepository;
import com.example.marketplace.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository,
                          ItemRepository itemRepository,
                          UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    public ResponseEntity<?> createMessage(Long itemId, Long senderId, MessageRequest request) {
        Item item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            return ResponseEntity.status(404).body(Map.of("error", "商品不存在"));
        }
        if (!StringUtils.hasText(request.getContent())) {
            return ResponseEntity.badRequest().body(Map.of("error", "留言内容不能为空"));
        }

        Message message = new Message();
        message.setItemId(itemId);
        message.setSenderId(senderId);
        message.setReceiverId(item.getSellerId());
        message.setContent(request.getContent().trim());

        message = messageRepository.save(message);
        return ResponseEntity.ok(toDto(message));
    }

    public ResponseEntity<List<MessageDto>> getItemMessages(Long itemId) {
        List<Message> messages = messageRepository.findByItemIdOrderByCreatedAtAsc(itemId);
        List<MessageDto> result = messages.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    private MessageDto toDto(Message message) {
        MessageDto dto = new MessageDto();
        dto.setId(message.getId());
        dto.setItemId(message.getItemId());
        dto.setSenderId(message.getSenderId());
        dto.setReceiverId(message.getReceiverId());
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());

        User sender = userRepository.findById(message.getSenderId()).orElse(null);
        if (sender != null) {
            dto.setSenderName(sender.getUsername());
        }
        User receiver = userRepository.findById(message.getReceiverId()).orElse(null);
        if (receiver != null) {
            dto.setReceiverName(receiver.getUsername());
        }
        return dto;
    }
}

