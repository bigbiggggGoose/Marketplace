package com.example.marketplace.controller;

import com.example.marketplace.dto.ReportRequest;
import com.example.marketplace.service.ReportService;
import com.example.marketplace.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.marketplace.repository.UserRepository;
import com.example.marketplace.model.User;

@RestController
@RequestMapping("/api/items")
public class MarketplaceReportController {
    private final ReportService reportService;
    private final UserRepository userRepository;

    public MarketplaceReportController(ReportService reportService, UserRepository userRepository) {
        this.reportService = reportService;
        this.userRepository = userRepository;
    }

    @PostMapping("/{itemId}/report")
    public ResponseEntity<?> createReport(
            @PathVariable Long itemId,
            @RequestBody ReportRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "未授权"));
        }
        return reportService.createReport(itemId, userId, request);
    }

    @GetMapping("/reports")
    public ResponseEntity<?> getReports(
            @RequestParam(defaultValue = "PENDING") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest httpRequest) {
        // 检查是否为管理员
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "未授权"));
        }
        if (!isAdmin(userId)) {
            return ResponseEntity.status(403).body(java.util.Map.of("error", "无权访问"));
        }
        return reportService.getReports(status, page, pageSize);
    }

    @PostMapping("/reports/{reportId}/process")
    public ResponseEntity<?> processReport(
            @PathVariable Long reportId,
            @RequestParam String action,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "未授权"));
        }
        if (!isAdmin(userId)) {
            return ResponseEntity.status(403).body(java.util.Map.of("error", "无权访问"));
        }
        return reportService.processReport(reportId, action);
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

    private boolean isAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        User user = userRepository.findById(userId).orElse(null);
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }
}

