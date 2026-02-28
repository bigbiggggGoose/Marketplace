package com.example.marketplace.service;

import com.example.marketplace.dto.ItemCreateRequest;
import com.example.marketplace.dto.ItemDto;
import com.example.marketplace.dto.ItemQueryRequest;
import com.example.marketplace.dto.ItemUpdateRequest;
import com.example.marketplace.dto.PageResult;
import com.example.marketplace.model.Item;
import com.example.marketplace.model.TradeRequest;
import com.example.marketplace.model.User;
import com.example.marketplace.repository.FavoriteRepository;
import com.example.marketplace.repository.ItemRepository;
import com.example.marketplace.repository.MessageRepository;
import com.example.marketplace.repository.ReportRepository;
import com.example.marketplace.repository.TradeMessageRepository;
import com.example.marketplace.repository.TradeRequestRepository;
import com.example.marketplace.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final TradeRequestRepository tradeRequestRepository;
    private final TradeMessageRepository tradeMessageRepository;
    private final ReportRepository reportRepository;
    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ItemService(ItemRepository itemRepository,
                       UserRepository userRepository,
                       FavoriteRepository favoriteRepository,
                       TradeRequestRepository tradeRequestRepository,
                       TradeMessageRepository tradeMessageRepository,
                       ReportRepository reportRepository,
                       MessageRepository messageRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.favoriteRepository = favoriteRepository;
        this.tradeRequestRepository = tradeRequestRepository;
        this.tradeMessageRepository = tradeMessageRepository;
        this.reportRepository = reportRepository;
        this.messageRepository = messageRepository;
    }

    @CacheEvict(cacheNames = {
        "items:detail",
        "items:list",
        "items:recommend",
        "items:userItems",
        "items:pendingReview",
        "items:adminList"
    }, allEntries = true)
    public ResponseEntity<?> createItem(ItemCreateRequest request, Long sellerId) {
        Item item = new Item();
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        Integer stock = request.getStock() == null ? 0 : request.getStock();
        item.setStock(stock);
        item.setCategory(request.getCategory());
        
        try {
            String imagesJson = objectMapper.writeValueAsString(request.getImages());
            item.setImages(imagesJson);
        } catch (Exception e) {
            item.setImages("[]");
        }
        
        item.setSellerId(sellerId);
        item.setStatus(stock > 0 ? "ON_SALE" : "OUT_OF_STOCK");
        item.setReviewStatus("PENDING");

        item = itemRepository.save(item);
        return ResponseEntity.ok(toItemDto(item));
    }

    // 修改：返回 PageResult<ItemDto>，不返回 ResponseEntity
    @Cacheable(cacheNames = "items:list", key = "T(String).valueOf(#request.keyword) + ':' + T(String).valueOf(#request.category) + ':' + T(String).valueOf(#request.sort) + ':' + T(String).valueOf(#request.page) + ':' + T(String).valueOf(#request.pageSize)")
    public PageResult<ItemDto> queryItemsData(ItemQueryRequest request) {
        // Map sort keys from frontend to entity fields
        String sortKey = request.getSort() == null ? "createdAt" : request.getSort();
        Sort.Direction direction = Sort.Direction.DESC;
        String sortProp = "createdAt";
        switch (sortKey) {
            case "price_asc" -> {
                direction = Sort.Direction.ASC;
                sortProp = "price";
            }
            case "price" -> {
                direction = Sort.Direction.DESC;
                sortProp = "price";
            }
            case "views" -> {
                direction = Sort.Direction.DESC;
                sortProp = "views";
            }
            case "createdAt", "new" -> {
                direction = Sort.Direction.DESC;
                sortProp = "createdAt";
            }
            default -> {
                sortProp = "createdAt";
            }
        }
        int pageIndex = request.getPage() == null ? 0 : Math.max(0, request.getPage() - 1);
        int pageSize = request.getPageSize() == null ? 20 : request.getPageSize();
        Pageable pageable = PageRequest.of(pageIndex, pageSize, Sort.by(direction, sortProp));

        String kw = StringUtils.hasText(request.getKeyword()) ? request.getKeyword() : null;
        String cat = StringUtils.hasText(request.getCategory()) ? request.getCategory() : null;

        Page<Item> page = itemRepository.searchItems(
            kw,
            cat,
            pageable
        );

        List<ItemDto> items = page.getContent().stream()
            .map(this::toItemDto)
            .collect(Collectors.toList());

        return new PageResult<>(
            items,
            page.getTotalElements(),
            request.getPage(),
            request.getPageSize()
        );
    }

    // 修改：返回 ItemDto，不返回 ResponseEntity
    @Cacheable(cacheNames = "items:detail", key = "#id")
    public ItemDto getItemByIdData(Long id) {
        Item item = itemRepository.findById(id).orElse(null);
        if (item == null) {
            return null;
        }

        // 增加浏览量
        item.setViews(item.getViews() + 1);
        itemRepository.save(item);

        return toItemDto(item);
    }

    // 修改：返回 List<ItemDto>，不返回 ResponseEntity
    @Cacheable(cacheNames = "items:recommend", key = "#limit")
    public List<ItemDto> getRecommendedItemsData(int limit) {
        try {
            int fetchSize = Math.max(limit * 3, limit);
            Pageable pageable = PageRequest.of(0, fetchSize);
            List<Item> items = itemRepository.findApprovedItemsForRecommendation(pageable);
            
            if (items.isEmpty()) {
                // 如果没有已审核的商品，尝试返回所有在售商品（包括未审核的，用于测试）
                Pageable fallbackPageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
                items = itemRepository.searchItems(null, null, fallbackPageable).getContent();
            }
            
            if (!items.isEmpty()) {
                Collections.shuffle(items);
                if (items.size() > limit) {
                    items = items.subList(0, limit);
                }
            }

            return items.stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // 修改：返回 List<ItemDto>，不返回 ResponseEntity
    @Cacheable(cacheNames = "items:userItems", key = "#userId")
    public List<ItemDto> getUserItemsData(Long userId) {
        List<Item> items = itemRepository.findBySellerIdOrderByCreatedAtDesc(userId);
        return items.stream()
            .map(this::toItemDto)
            .collect(Collectors.toList());
    }

    // Controller 使用的包装方法
    public ResponseEntity<List<ItemDto>> getUserItems(Long userId) {
        List<ItemDto> result = getUserItemsData(userId);
        return ResponseEntity.ok(result);
    }

    // 修改：返回 List<ItemDto>，不返回 ResponseEntity
    @Cacheable(cacheNames = "items:pendingReview", key = "'all'")
    public List<ItemDto> getItemsPendingReviewData() {
        List<Item> items = itemRepository.findByReviewStatusOrderByCreatedAtDesc("PENDING");
        return items.stream()
            .map(this::toItemDto)
            .collect(Collectors.toList());
    }

    // Controller 使用的包装方法
    public ResponseEntity<List<ItemDto>> getItemsPendingReview() {
        List<ItemDto> result = getItemsPendingReviewData();
        return ResponseEntity.ok(result);
    }

    // 修改：返回 PageResult<ItemDto>，不返回 ResponseEntity
    @Cacheable(cacheNames = "items:adminList", key = "T(String).valueOf(#keyword) + ':' + T(String).valueOf(#status) + ':' + T(String).valueOf(#reviewStatus) + ':' + #page + ':' + #pageSize")
    public PageResult<ItemDto> getAdminItemsData(String keyword, String status, String reviewStatus, int page, int pageSize) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(pageSize, 1), Sort.by(Sort.Direction.DESC, "createdAt"));
        String kw = StringUtils.hasText(keyword) ? keyword : null;
        String st = StringUtils.hasText(status) ? status : null;
        String rv = StringUtils.hasText(reviewStatus) ? reviewStatus : null;

        Page<Item> resultPage = itemRepository.adminSearchItems(kw, st, rv, pageable);
        List<ItemDto> items = resultPage.getContent().stream()
            .map(this::toItemDto)
            .collect(Collectors.toList());

        return new PageResult<>(
            items,
            resultPage.getTotalElements(),
            page + 1,
            pageSize
        );
    }

    // Controller 使用的包装方法
    public ResponseEntity<PageResult<ItemDto>> getAdminItems(String keyword, String status, String reviewStatus, int page, int pageSize) {
        PageResult<ItemDto> result = getAdminItemsData(keyword, status, reviewStatus, page, pageSize);
        return ResponseEntity.ok(result);
    }

    @CacheEvict(cacheNames = {
        "items:detail",
        "items:list",
        "items:recommend",
        "items:userItems",
        "items:pendingReview",
        "items:adminList"
    }, allEntries = true)
    public ResponseEntity<?> adminProcessItem(Long itemId, String action) {
        Item item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "商品不存在"));
        }

        switch (action) {
            case "approve" -> item.setReviewStatus("APPROVED");
            case "take_down" -> {
                item.setStatus("OFFLINE");
                item.setReviewStatus("RESOLVED");
            }
            case "mark_resolved" -> item.setReviewStatus("RESOLVED");
            case "put_on" -> {
                item.setStatus("ON_SALE");
                item.setReviewStatus("APPROVED");
            }
            default -> {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "无效操作"));
            }
        }

        itemRepository.save(item);
        return ResponseEntity.ok(toItemDto(item));
    }

    @CacheEvict(cacheNames = {
        "items:detail",
        "items:list",
        "items:recommend",
        "items:userItems",
        "items:pendingReview",
        "items:adminList"
    }, allEntries = true)
    public ResponseEntity<?> updateItem(Long itemId, Long userId, ItemUpdateRequest request) {
        Item item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "商品不存在"));
        }

        if (!item.getSellerId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "无权修改此商品"));
        }

        if (request.getTitle() != null) item.setTitle(request.getTitle());
        if (request.getDescription() != null) item.setDescription(request.getDescription());
        if (request.getPrice() != null) item.setPrice(request.getPrice());
        boolean stockUpdated = false;
        if (request.getStock() != null) {
            item.setStock(request.getStock());
            stockUpdated = true;
        }
        if (request.getCategory() != null) item.setCategory(request.getCategory());
        if (request.getStatus() != null) {
            Set<String> allowedStatus = Set.of("ON_SALE", "OUT_OF_STOCK", "OFFLINE");
            if (allowedStatus.contains(request.getStatus())) {
                item.setStatus(request.getStatus());
            }
        } else if (stockUpdated) {
            item.setStatus(item.getStock() != null && item.getStock() > 0 ? "ON_SALE" : "OUT_OF_STOCK");
        }

        if (request.getImages() != null) {
            try {
                String imagesJson = objectMapper.writeValueAsString(request.getImages());
                item.setImages(imagesJson);
            } catch (Exception e) {
                // 忽略JSON序列化错误
            }
        }

        item = itemRepository.save(item);
        return ResponseEntity.ok(toItemDto(item));
    }

    @CacheEvict(cacheNames = {
        "items:detail",
        "items:list",
        "items:recommend",
        "items:userItems",
        "items:pendingReview",
        "items:adminList"
    }, allEntries = true)
    public ResponseEntity<?> deleteItem(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "商品不存在"));
        }

        if (!item.getSellerId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "无权删除此商品"));
        }

        cleanupAssociations(itemId);
        itemRepository.delete(item);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    private void cleanupAssociations(Long itemId) {
        List<TradeRequest> requests = tradeRequestRepository.findByItemId(itemId);
        if (!requests.isEmpty()) {
            List<Long> ids = requests.stream()
                .map(TradeRequest::getId)
                .collect(Collectors.toList());
            tradeMessageRepository.deleteByTradeRequestIdIn(ids);
            tradeRequestRepository.deleteAll(requests);
        }
        favoriteRepository.deleteByItemId(itemId);
        reportRepository.deleteByItemId(itemId);
        messageRepository.deleteByItemId(itemId);
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
        dto.setStatus(normalizeStatus(item.getStatus(), item.getStock()));
        dto.setReviewStatus(item.getReviewStatus());
        dto.setCreatedAt(item.getCreatedAt());

        // 解析图片JSON
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

        // 获取卖家名称
        if (item.getSeller() != null) {
            dto.setSellerName(item.getSeller().getUsername());
        } else {
            User seller = userRepository.findById(item.getSellerId()).orElse(null);
            if (seller != null) {
                dto.setSellerName(seller.getUsername());
            }
        }

        return dto;
    }

    private String normalizeStatus(String status, Integer stock) {
        if (status == null) {
            return stock != null && stock > 0 ? "ON_SALE" : "OUT_OF_STOCK";
        }
        return switch (status) {
            case "ON_SALE", "OUT_OF_STOCK", "OFFLINE" -> status;
            case "APPROVED" -> "ON_SALE";
            case "SOLD" -> "OFFLINE";
            case "PENDING", "REJECTED" -> status;
            default -> stock != null && stock > 0 ? "ON_SALE" : "OUT_OF_STOCK";
        };
    }
}