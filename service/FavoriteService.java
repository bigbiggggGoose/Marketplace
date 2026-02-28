package com.example.marketplace.service;

import com.example.marketplace.dto.ItemDto;
import com.example.marketplace.model.Favorite;
import com.example.marketplace.model.Item;
import com.example.marketplace.repository.FavoriteRepository;
import com.example.marketplace.repository.ItemRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final ItemRepository itemRepository;
    private final ObjectMapper objectMapper;

    public FavoriteService(FavoriteRepository favoriteRepository,
                          ItemRepository itemRepository,
                          ObjectMapper objectMapper) {
        this.favoriteRepository = favoriteRepository;
        this.itemRepository = itemRepository;
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<?> toggleFavorite(Long userId, Long itemId) {
        Favorite existing = favoriteRepository.findByUserIdAndItemId(userId, itemId).orElse(null);
        if (existing != null) {
            favoriteRepository.delete(existing);
            return ResponseEntity.ok(Map.of("message", "已取消收藏", "favorited", false));
        } else {
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setItemId(itemId);
            favoriteRepository.save(favorite);
            return ResponseEntity.ok(Map.of("message", "已收藏", "favorited", true));
        }
    }

    public ResponseEntity<Boolean> checkFavorite(Long userId, Long itemId) {
        boolean favorited = favoriteRepository.existsByUserIdAndItemId(userId, itemId);
        return ResponseEntity.ok(favorited);
    }

    public ResponseEntity<List<ItemDto>> getMyFavorites(Long userId) {
        List<Favorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<ItemDto> items = favorites.stream()
            .map(fav -> {
                Item item = itemRepository.findById(fav.getItemId()).orElse(null);
                if (item != null) {
                    return toItemDto(item);
                }
                return null;
            })
            .filter(item -> item != null)
            .collect(Collectors.toList());
        return ResponseEntity.ok(items);
    }

    private ItemDto toItemDto(Item item) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setTitle(item.getTitle());
        dto.setDescription(item.getDescription());
        dto.setPrice(item.getPrice());
        dto.setStock(item.getStock());
        dto.setCategory(item.getCategory());
        dto.setSellerId(item.getSellerId());
        dto.setViews(item.getViews());
        dto.setStatus(item.getStatus());
        dto.setReviewStatus(item.getReviewStatus());
        dto.setCreatedAt(item.getCreatedAt());

        try {
            if (item.getImages() != null && !item.getImages().isEmpty()) {
                List<String> images = objectMapper.readValue(
                    item.getImages(),
                    new TypeReference<List<String>>() {}
                );
                dto.setImages(images);
            } else {
                dto.setImages(List.of());
            }
        } catch (Exception e) {
            dto.setImages(List.of());
        }

        return dto;
    }
}

