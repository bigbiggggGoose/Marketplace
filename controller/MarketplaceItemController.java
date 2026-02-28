package com.example.marketplace.controller;

import com.example.marketplace.dto.ItemCreateRequest;
import com.example.marketplace.dto.ItemDto;
import com.example.marketplace.dto.ItemQueryRequest;
import com.example.marketplace.dto.ItemUpdateRequest;
import com.example.marketplace.dto.PageResult;
import com.example.marketplace.service.ItemService;
import com.example.marketplace.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
            return ResponseEntity.status(401).body(Map.of("error", "未授权"));
        }
        return itemService.createItem(request, userId);
    }

    @PostMapping("/query")
    public ResponseEntity<PageResult<ItemDto>> queryItems(@RequestBody ItemQueryRequest request) {
        PageResult<ItemDto> result = itemService.queryItemsData(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<PageResult<ItemDto>> queryItemsByGet(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        ItemQueryRequest req = new ItemQueryRequest();
        req.setKeyword(keyword);
        req.setCategory(category);
        req.setSort(sort);
        req.setPage(page);
        req.setPageSize(pageSize);
        PageResult<ItemDto> result = itemService.queryItemsData(req);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> getItem(@PathVariable Long id) {
        ItemDto item = itemService.getItemByIdData(id);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }

    @GetMapping("/recommended")
    public ResponseEntity<List<ItemDto>> getRecommended(@RequestParam(defaultValue = "10") int limit) {
        List<ItemDto> items = itemService.getRecommendedItemsData(limit);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ItemDto>> getMyItems(HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        List<ItemDto> items = itemService.getUserItemsData(userId);
        return ResponseEntity.ok(items);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(
            @PathVariable Long id,
            @RequestBody ItemUpdateRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未授权"));
        }
        return itemService.updateItem(id, userId, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "未授权"));
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