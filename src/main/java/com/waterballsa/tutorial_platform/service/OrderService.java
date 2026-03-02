package com.waterballsa.tutorial_platform.service;

import com.waterballsa.tutorial_platform.dto.OrderDTO;
import com.waterballsa.tutorial_platform.entity.Announcement;
import com.waterballsa.tutorial_platform.entity.Order;
import com.waterballsa.tutorial_platform.entity.Journey;
import com.waterballsa.tutorial_platform.repository.AnnouncementRepository;
import com.waterballsa.tutorial_platform.repository.JourneyRepository;
import com.waterballsa.tutorial_platform.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final JourneyRepository journeyRepository;
    private final AnnouncementRepository announcementRepository;

    @Transactional
    public OrderDTO createOrder(Long userId, Long journeyId, Long amount, String paymentMethod, String invoiceType, String invoiceValue) {
        String orderNumber = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .userId(userId)
                .journeyId(journeyId)
                .amount(amount)
                .status(Order.OrderStatus.PENDING)
                .paymentMethod(paymentMethod)
                .invoiceType(invoiceType)
                .invoiceValue(invoiceValue)
                .createdAt(LocalDateTime.now())
                .build();
        
        Order savedOrder = orderRepository.save(order);

        // 自動建立通知
        announcementRepository.save(Announcement.builder()
                .userId(userId)
                .message("🎉 您的訂單 " + orderNumber + " 已建立成功！請前往完成支付。")
                .linkText("查看訂單")
                .linkHref("/courses")
                .build());

        return toDto(savedOrder);
    }

    public List<OrderDTO> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDTO markAsPaid(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));
        order.setStatus(Order.OrderStatus.PAID);
        order.setPaidAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        // 自動建立通知
        Journey journey = journeyRepository.findById(order.getJourneyId()).orElse(null);
        String journeySlug = journey != null ? journey.getSlug() : "software-design-pattern";
        
        announcementRepository.save(Announcement.builder()
                .userId(order.getUserId())
                .message("✅ 您的訂單 " + orderNumber + " 已完成支付！快去開始您的學習旅程吧。")
                .linkText("前往挑戰地圖")
                .linkHref("/journeys/" + journeySlug + "/map")
                .build());

        return toDto(savedOrder);
    }

    private OrderDTO toDto(Order order) {
        Journey journey = journeyRepository.findById(order.getJourneyId())
                .orElse(null);
        String journeyName = journey != null ? journey.getName() : "Unknown Journey";
        String journeySlug = journey != null ? journey.getSlug() : "";

        return OrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .journeyId(order.getJourneyId())
                .journeyName(journeyName)
                .journeySlug(journeySlug)
                .amount(order.getAmount())
                .status(order.getStatus().name())
                .paymentMethod(order.getPaymentMethod())
                .paymentDetails(order.getPaymentDetails())
                .invoiceType(order.getInvoiceType())
                .invoiceValue(order.getInvoiceValue())
                .createdAt(order.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
                .paidAt(order.getPaidAt() != null ? order.getPaidAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() : null)
                .build();
    }
}
