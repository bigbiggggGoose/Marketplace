package com.example.marketplace.controller;

import com.example.marketplace.model.User;
import com.example.marketplace.service.ItemService;
import com.example.marketplace.service.ReportService;
import com.example.marketplace.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ItemService itemService;
    private final ReportService reportService;
    private final com.example.marketplace.repository.UserRepository userRepository;

    public AdminController(ItemService itemService,
                           ReportService reportService,
                           com.example.marketplace.repository.UserRepository userRepository) {
        this.itemService = itemService;
        this.reportService = reportService;
        this.userRepository = userRepository;
    }

    @GetMapping("/items/review")
    public ResponseEntity<?> getPendingItems(HttpServletRequest request) {
        User admin = requireAdmin(request);
        if (admin == null) {
            return ResponseEntity.status(403).body(Map.of("error", "无权访问"));
        }
        return itemService.getItemsPendingReview();
    }

    @PostMapping("/items/{itemId}/review")
    public ResponseEntity<?> processItem(@PathVariable Long itemId,
                                         @RequestParam String action,
                                         HttpServletRequest request) {
        User admin = requireAdmin(request);
        if (admin == null) {
            return ResponseEntity.status(403).body(Map.of("error", "无权访问"));
        }
        return itemService.adminProcessItem(itemId, action);
    }

    @GetMapping("/items")
    public ResponseEntity<?> getAllItems(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) String status,
                                         @RequestParam(required = false, name = "reviewStatus") String reviewStatus,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "20") int pageSize,
                                         HttpServletRequest request) {
        User admin = requireAdmin(request);
        if (admin == null) {
            return ResponseEntity.status(403).body(Map.of("error", "无权访问"));
        }
        return itemService.getAdminItems(keyword, status, reviewStatus, Math.max(page - 1, 0), pageSize);
    }

    @GetMapping("/reports")
    public ResponseEntity<?> getReports(@RequestParam(defaultValue = "PENDING") String status,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int pageSize,
                                        HttpServletRequest request) {
        User admin = requireAdmin(request);
        if (admin == null) {
            return ResponseEntity.status(403).body(Map.of("error", "无权访问"));
        }
        return reportService.getReports(status, page, pageSize);
    }

    @PostMapping("/reports/{reportId}/process")
    public ResponseEntity<?> processReport(@PathVariable Long reportId,
                                           @RequestParam String action,
                                           HttpServletRequest request) {
        User admin = requireAdmin(request);
        if (admin == null) {
            return ResponseEntity.status(403).body(Map.of("error", "无权访问"));
        }
        return reportService.processReport(reportId, action);
    }

    private User requireAdmin(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || !JwtUtil.validateToken(token)) {
            return null;
        }
        Long userId = JwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return null;
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            return null;
        }
        return user;
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
