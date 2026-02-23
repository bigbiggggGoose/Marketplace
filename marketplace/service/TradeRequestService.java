package com.example.marketplace.service;

import com.example.marketplace.dto.TradeRequestCreateRequest;
import com.example.marketplace.dto.TradeRequestDto;
import com.example.marketplace.dto.TradeMessageDto;
import com.example.marketplace.dto.TradeMessageRequest;
import com.example.marketplace.model.Item;
import com.example.marketplace.model.TradeRequest;
import com.example.marketplace.model.TradeMessage;
import com.example.marketplace.model.User;
import com.example.marketplace.repository.ItemRepository;
import com.example.marketplace.repository.TradeRequestRepository;
import com.example.marketplace.repository.TradeMessageRepository;
import com.example.marketplace.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TradeRequestService {
    private final TradeRequestRepository tradeRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final TradeMessageRepository tradeMessageRepository;

    public TradeRequestService(TradeRequestRepository tradeRequestRepository,
                               ItemRepository itemRepository,
                               UserRepository userRepository,
                               TradeMessageRepository tradeMessageRepository) {
        this.tradeRequestRepository = tradeRequestRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.tradeMessageRepository = tradeMessageRepository;
    }

    public ResponseEntity<?> createTradeRequest(Long itemId, Long buyerId, TradeRequestCreateRequest request) {
        Item item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            return ResponseEntity.status(404).body(Map.of("error", "商品不存在"));
        }

        if (item.getSellerId().equals(buyerId)) {
            return ResponseEntity.status(400).body(Map.of("error", "不能申请购买自己的商品"));
        }

        // 检查是否已有待处理的申请
        List<TradeRequest> existing = tradeRequestRepository.findByItemIdAndBuyerId(itemId, buyerId);
        boolean hasPending = existing.stream().anyMatch(tr -> "PENDING".equals(tr.getStatus()));
        if (hasPending) {
            return ResponseEntity.status(400).body(Map.of("error", "您已提交过交易申请，请等待卖家处理"));
        }

        int quantity = request.getQuantity() == null ? 1 : Math.max(1, request.getQuantity());
        if (item.getStock() != null && item.getStock() > 0 && quantity > item.getStock()) {
            return ResponseEntity.status(400).body(Map.of("error", "申请数量超过库存"));
        }

        TradeRequest tradeRequest = new TradeRequest();
        tradeRequest.setItemId(itemId);
        tradeRequest.setBuyerId(buyerId);
        tradeRequest.setSellerId(item.getSellerId());
        tradeRequest.setMessage(request.getMessage());
        tradeRequest.setQuantity(quantity);
        tradeRequest.setStatus("PENDING");

        tradeRequest = tradeRequestRepository.save(tradeRequest);
        
        // 如果用户填写了留言，创建一条交易消息
        if (StringUtils.hasText(request.getMessage())) {
            TradeMessage initialMessage = new TradeMessage();
            initialMessage.setTradeRequestId(tradeRequest.getId());
            initialMessage.setSenderId(buyerId);
            initialMessage.setContent(request.getMessage().trim());
            tradeMessageRepository.save(initialMessage);
        }
        
        return ResponseEntity.ok(toDto(tradeRequest));
    }

    public ResponseEntity<List<TradeRequestDto>> getMyRequests(Long userId) {
        List<TradeRequest> requests = tradeRequestRepository.findByBuyerIdOrderByCreatedAtDesc(userId);
        List<TradeRequestDto> result = requests.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    public ResponseEntity<List<TradeRequestDto>> getReceivedRequests(Long userId) {
        List<TradeRequest> requests = tradeRequestRepository.findBySellerIdOrderByCreatedAtDesc(userId);
        List<TradeRequestDto> result = requests.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    public ResponseEntity<?> processRequest(Long requestId, Long sellerId, String action) {
        TradeRequest request = tradeRequestRepository.findById(requestId).orElse(null);
        if (request == null) {
            return ResponseEntity.status(404).body(Map.of("error", "申请不存在"));
        }

        if (!request.getSellerId().equals(sellerId)) {
            return ResponseEntity.status(403).body(Map.of("error", "无权处理此申请"));
        }

        if (!"PENDING".equals(request.getStatus())) {
            return ResponseEntity.status(400).body(Map.of("error", "申请已处理"));
        }

        if ("accept".equals(action)) {
            request.setStatus("ACCEPTED");
        } else if ("reject".equals(action)) {
            request.setStatus("REJECTED");
        } else {
            return ResponseEntity.status(400).body(Map.of("error", "无效的操作"));
        }

        tradeRequestRepository.save(request);
        return ResponseEntity.ok(Map.of("message", "处理成功"));
    }

    public ResponseEntity<?> sendSellerOffer(Long requestId, Long sellerId, com.example.marketplace.dto.SellerOfferRequest offerRequest) {
        TradeRequest request = tradeRequestRepository.findById(requestId).orElse(null);
        if (request == null) {
            return ResponseEntity.status(404).body(Map.of("error", "申请不存在"));
        }

        if (!request.getSellerId().equals(sellerId)) {
            return ResponseEntity.status(403).body(Map.of("error", "无权处理此申请"));
        }

        if (!"ACCEPTED".equals(request.getStatus())) {
            return ResponseEntity.status(400).body(Map.of("error", "只能对已接受的申请发送报价"));
        }

        Item item = itemRepository.findById(request.getItemId()).orElse(null);
        if (item == null) {
            return ResponseEntity.status(404).body(Map.of("error", "商品不存在"));
        }

        // 验证数量不超过库存
        int offerQuantity = offerRequest.getQuantity() == null ? request.getQuantity() : offerRequest.getQuantity();
        if (item.getStock() != null && item.getStock() > 0 && offerQuantity > item.getStock()) {
            return ResponseEntity.status(400).body(Map.of("error", "报价数量超过库存"));
        }

        request.setSellerOfferPrice(offerRequest.getPrice());
        request.setSellerOfferQuantity(offerQuantity);
        request.setStatus("SELLER_OFFERED");
        tradeRequestRepository.save(request);

        // 如果有留言，创建消息
        if (offerRequest.getMessage() != null && !offerRequest.getMessage().trim().isEmpty()) {
            // 这里可以调用MessageService创建消息，暂时先保存到trade request的message字段
            String existingMessage = request.getMessage() != null ? request.getMessage() + "\n\n" : "";
            request.setMessage(existingMessage + "[商家报价] " + offerRequest.getMessage());
            tradeRequestRepository.save(request);
        }

        return ResponseEntity.ok(toDto(request));
    }

    public ResponseEntity<?> confirmPayment(Long requestId, Long buyerId) {
        TradeRequest request = tradeRequestRepository.findById(requestId).orElse(null);
        if (request == null) {
            return ResponseEntity.status(404).body(Map.of("error", "申请不存在"));
        }

        if (!request.getBuyerId().equals(buyerId)) {
            return ResponseEntity.status(403).body(Map.of("error", "无权确认此交易"));
        }

        if (!"SELLER_OFFERED".equals(request.getStatus())) {
            return ResponseEntity.status(400).body(Map.of("error", "只能确认商家已报价的交易"));
        }

        request.setBuyerConfirmed(true);
        request.setStatus("BUYER_CONFIRMED");

        // 扣除库存
        Item item = itemRepository.findById(request.getItemId()).orElse(null);
        if (item != null) {
            int quantity = request.getSellerOfferQuantity() != null ? request.getSellerOfferQuantity() : request.getQuantity();
            int currentStock = item.getStock() == null ? 0 : item.getStock();
            int newStock = Math.max(0, currentStock - quantity);
            item.setStock(newStock);
            if (newStock <= 0) {
                item.setStatus("OUT_OF_STOCK");
            }
            itemRepository.save(item);
        }

        tradeRequestRepository.save(request);
        return ResponseEntity.ok(Map.of("message", "交易确认成功", "tradeRequest", toDto(request)));
    }

    private TradeRequestDto toDto(TradeRequest request) {
        TradeRequestDto dto = new TradeRequestDto();
        dto.setId(request.getId());
        dto.setItemId(request.getItemId());
        dto.setBuyerId(request.getBuyerId());
        dto.setSellerId(request.getSellerId());
        dto.setMessage(request.getMessage());
        dto.setQuantity(request.getQuantity());
        dto.setSellerOfferPrice(request.getSellerOfferPrice());
        dto.setSellerOfferQuantity(request.getSellerOfferQuantity());
        dto.setBuyerConfirmed(request.getBuyerConfirmed());
        dto.setStatus(request.getStatus());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setUpdatedAt(request.getUpdatedAt());

        Item item = itemRepository.findById(request.getItemId()).orElse(null);
        if (item != null) {
            dto.setItemTitle(item.getTitle());
        }

        User buyer = userRepository.findById(request.getBuyerId()).orElse(null);
        if (buyer != null) {
            dto.setBuyerName(buyer.getUsername());
        }

        User seller = userRepository.findById(request.getSellerId()).orElse(null);
        if (seller != null) {
            dto.setSellerName(seller.getUsername());
        }

        return dto;
    }

    public ResponseEntity<?> sendTradeMessage(Long tradeRequestId, Long userId, TradeMessageRequest request) {
        TradeRequest tradeRequest = tradeRequestRepository.findById(tradeRequestId).orElse(null);
        if (tradeRequest == null) {
            return ResponseEntity.status(404).body(Map.of("error", "交易申请不存在"));
        }

        // 验证用户是否有权限发送消息（必须是买家或卖家）
        if (!tradeRequest.getBuyerId().equals(userId) && !tradeRequest.getSellerId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "无权发送消息"));
        }

        if (!StringUtils.hasText(request.getContent())) {
            return ResponseEntity.badRequest().body(Map.of("error", "消息内容不能为空"));
        }

        TradeMessage message = new TradeMessage();
        message.setTradeRequestId(tradeRequestId);
        message.setSenderId(userId);
        message.setContent(request.getContent().trim());

        message = tradeMessageRepository.save(message);
        return ResponseEntity.ok(toMessageDto(message));
    }

    public ResponseEntity<List<TradeMessageDto>> getTradeMessages(Long tradeRequestId, Long userId) {
        TradeRequest tradeRequest = tradeRequestRepository.findById(tradeRequestId).orElse(null);
        if (tradeRequest == null) {
            return ResponseEntity.status(404).body(List.of());
        }

        // 验证用户是否有权限查看消息（必须是买家或卖家）
        if (!tradeRequest.getBuyerId().equals(userId) && !tradeRequest.getSellerId().equals(userId)) {
            return ResponseEntity.status(403).body(List.of());
        }

        List<TradeMessage> messages = tradeMessageRepository.findByTradeRequestIdOrderByCreatedAtAsc(tradeRequestId);
        List<TradeMessageDto> result = messages.stream()
            .map(this::toMessageDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    private TradeMessageDto toMessageDto(TradeMessage message) {
        TradeMessageDto dto = new TradeMessageDto();
        dto.setId(message.getId());
        dto.setTradeRequestId(message.getTradeRequestId());
        dto.setSenderId(message.getSenderId());
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());

        User sender = userRepository.findById(message.getSenderId()).orElse(null);
        if (sender != null) {
            dto.setSenderName(sender.getUsername());
        }

        return dto;
    }
}

