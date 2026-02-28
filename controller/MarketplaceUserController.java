package com.example.marketplace.controller;

import com.example.marketplace.dto.UserDto;
import com.example.marketplace.service.AuthService;
import com.example.marketplace.service.ItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class MarketplaceUserController {
    private final AuthService authService;
    private final ItemService itemService;

    public MarketplaceUserController(AuthService authService, ItemService itemService) {
        this.authService = authService;
        this.itemService = itemService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        UserDto dto = authService.getUserByIdData(id);
        if (dto == null) {
            return ResponseEntity.status(404).body(java.util.Map.of("error", "用户不存在"));
        }
        dto.setPhone(maskPhone(dto.getPhone()));
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<?> getUserItems(@PathVariable Long id) {
        return itemService.getUserItems(id);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return phone;
        }
        return phone.substring(0, phone.length() - 4) + "****";
    }
}

