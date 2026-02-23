package com.example.marketplace.controller;

import com.example.marketplace.dto.LoginRequest;
import com.example.marketplace.dto.RegisterRequest;
import com.example.marketplace.service.AuthService;
import com.example.marketplace.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class MarketplaceAuthController {
    private final AuthService authService;

    public MarketplaceAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || !JwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "未授权"));
        }

        Long userId = JwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "无效token"));
        }

        return ResponseEntity.ok(authService.getUserById(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateMe(
            HttpServletRequest httpRequest,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone) {
        String token = extractToken(httpRequest);
        if (token == null || !JwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "未授权"));
        }

        Long userId = JwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "无效token"));
        }

        return authService.updateUser(userId, email, phone);
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}

