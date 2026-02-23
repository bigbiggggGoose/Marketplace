package com.example.marketplace.controller;

import com.example.marketplace.dto.MessageRequest;
import com.example.marketplace.service.MessageService;
import com.example.marketplace.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")
public class MarketplaceMessageController {
    private final MessageService messageService;

    public MarketplaceMessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/{itemId}/messages")
    public ResponseEntity<?> createMessage(
            @PathVariable Long itemId,
            @RequestBody MessageRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "未授权"));
        }
        return messageService.createMessage(itemId, userId, request);
    }

    @GetMapping("/{itemId}/messages")
    public ResponseEntity<?> getMessages(@PathVariable Long itemId) {
        return messageService.getItemMessages(itemId);
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || !JwtUtil.validateToken(token)) {
            return null;
        }
        return JwtUtil.getUserIdFromToken(token);
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}

