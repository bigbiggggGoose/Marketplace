package com.example.marketplace.util;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {
    // 简单实现，生产环境建议使用 jjwt 库
    private static final String SECRET = "marketplace-secret-key-change-in-production";
    private static final long EXPIRATION_TIME = 86400000; // 24小时

    public static String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_TIME);
        
        // 简单实现：Base64编码的JSON
        String header = "{\"typ\":\"JWT\",\"alg\":\"HS256\"}";
        String payload = String.format("{\"userId\":%d,\"username\":\"%s\",\"exp\":%d}", 
                                      userId, username, expiry.getTime() / 1000);
        
        String encodedHeader = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(header.getBytes());
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(payload.getBytes());
        
        return encodedHeader + "." + encodedPayload + ".signature";
    }

    public static Long getUserIdFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;
            
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            // 简单解析，生产环境应使用 jjwt
            if (payload.contains("\"userId\"")) {
                int start = payload.indexOf("\"userId\":") + 9;
                int end = payload.indexOf(",", start);
                if (end == -1) end = payload.indexOf("}", start);
                return Long.parseLong(payload.substring(start, end).trim());
            }
        } catch (Exception e) {
            // 忽略解析错误
        }
        return null;
    }

    public static boolean validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            return parts.length == 3;
        } catch (Exception e) {
            return false;
        }
    }
}

