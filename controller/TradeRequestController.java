package com.example.marketplace.controller;

import com.example.marketplace.dto.TradeRequestCreateRequest;
import com.example.marketplace.dto.TradeRequestDto;
import com.example.marketplace.dto.TradeMessageDto;
import com.example.marketplace.dto.TradeMessageRequest;
import com.example.marketplace.service.TradeRequestService;
import com.example.marketplace.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trade")
public class TradeRequestController {
    private final TradeRequestService tradeRequestService;

    public TradeRequestController(TradeRequestService tradeRequestService) {
        this.tradeRequestService = tradeRequestService;
    }

    @PostMapping("/items/{itemId}/request")
    public ResponseEntity<?> createRequest(
            @PathVariable Long itemId,
            @RequestBody TradeRequestCreateRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未授权"));
        }
        return tradeRequestService.createTradeRequest(itemId, userId, request);
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<TradeRequestDto>> getMyRequests(HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        List<TradeRequestDto> requests = tradeRequestService.getMyRequestsData(userId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/received-requests")
    public ResponseEntity<List<TradeRequestDto>> getReceivedRequests(HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        List<TradeRequestDto> requests = tradeRequestService.getReceivedRequestsData(userId);
        return ResponseEntity.ok(requests);
    }

    @PostMapping("/requests/{requestId}/process")
    public ResponseEntity<?> processRequest(
            @PathVariable Long requestId,
            @RequestParam String action,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未授权"));
        }
        return tradeRequestService.processRequest(requestId, userId, action);
    }

    @PostMapping("/requests/{requestId}/offer")
    public ResponseEntity<?> sendSellerOffer(
            @PathVariable Long requestId,
            @RequestBody com.example.marketplace.dto.SellerOfferRequest offerRequest,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未授权"));
        }
        return tradeRequestService.sendSellerOffer(requestId, userId, offerRequest);
    }

    @PostMapping("/requests/{requestId}/confirm")
    public ResponseEntity<?> confirmPayment(
            @PathVariable Long requestId,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未授权"));
        }
        return tradeRequestService.confirmPayment(requestId, userId);
    }

    @PostMapping("/requests/{requestId}/messages")
    public ResponseEntity<TradeMessageDto> sendTradeMessage(
            @PathVariable Long requestId,
            @RequestBody TradeMessageRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        TradeMessageDto message = tradeRequestService.sendTradeMessageData(requestId, userId, request);
        if (message == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(message);
    }

    @GetMapping("/requests/{requestId}/messages")
    public ResponseEntity<List<TradeMessageDto>> getTradeMessages(
            @PathVariable Long requestId,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        List<TradeMessageDto> messages = tradeRequestService.getTradeMessagesData(requestId, userId);
        return ResponseEntity.ok(messages);
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