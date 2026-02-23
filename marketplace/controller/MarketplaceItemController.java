package com.example.marketplace.controller;

import com.example.marketplace.dto.ItemCreateRequest;
import com.example.marketplace.dto.ItemQueryRequest;
import com.example.marketplace.dto.ItemUpdateRequest;
import com.example.marketplace.service.ItemService;
import com.example.marketplace.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")
public class MarketplaceItemController {
    private final ItemService itemService;

    public MarketplaceItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ResponseEntity<?> createItem(@RequestBody ItemCreateRequest request, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "未授权"));
        }
        return itemService.createItem(request, userId);
    }

    @PostMapping("/query")
    public ResponseEntity<?> queryItems(@RequestBody ItemQueryRequest request) {
        return itemService.queryItems(request);
    }

    @GetMapping
    public ResponseEntity<?> queryItemsByGet(@RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String category,
                                             @RequestParam(defaultValue = "createdAt") String sort,
                                             @RequestParam(defaultValue = "0") Integer page,
                                             @RequestParam(defaultValue = "20") Integer pageSize) {
        ItemQueryRequest req = new ItemQueryRequest();
        req.setKeyword(keyword);
        req.setCategory(category);
        req.setSort(sort);
        req.setPage(page);
        req.setPageSize(pageSize);
        return itemService.queryItems(req);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getItem(@PathVariable Long id) {
        return itemService.getItemById(id);
    }

    @GetMapping("/recommended")
    public ResponseEntity<?> getRecommended(@RequestParam(defaultValue = "10") int limit) {
        return itemService.getRecommendedItems(limit);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyItems(HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "未授权"));
        }
        return itemService.getUserItems(userId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(
            @PathVariable Long id,
            @RequestBody ItemUpdateRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "未授权"));
        }
        return itemService.updateItem(id, userId, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "未授权"));
        }
        return itemService.deleteItem(id, userId);
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

