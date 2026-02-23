package com.example.marketplace.controller;

import com.example.marketplace.service.FavoriteService;
import com.example.marketplace.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {
    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/items/{itemId}/toggle")
    public ResponseEntity<?> toggleFavorite(
            @PathVariable Long itemId,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "未授权"));
        }
        return favoriteService.toggleFavorite(userId, itemId);
    }

    @GetMapping("/items/{itemId}/check")
    public ResponseEntity<?> checkFavorite(
            @PathVariable Long itemId,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ResponseEntity.ok(false);
        }
        return favoriteService.checkFavorite(userId, itemId);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyFavorites(HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "未授权"));
        }
        return favoriteService.getMyFavorites(userId);
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

