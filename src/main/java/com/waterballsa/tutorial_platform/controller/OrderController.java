package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.dto.OrderDTO;
import com.waterballsa.tutorial_platform.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/orders")
    public ResponseEntity<OrderDTO> createOrder(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        Long journeyId = Long.valueOf(request.get("journeyId").toString());
        Long amount = Long.valueOf(request.get("amount").toString());
        String paymentMethod = (String) request.get("paymentMethod");
        String invoiceType = (String) request.get("invoiceType");
        String invoiceValue = (String) request.get("invoiceValue");

        return ResponseEntity.ok(orderService.createOrder(userId, journeyId, amount, paymentMethod, invoiceType, invoiceValue));
    }

    @GetMapping("/users/{userId}/orders")
    public ResponseEntity<List<OrderDTO>> getUserOrders(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    @PostMapping("/orders/{orderNumber}/pay")
    public ResponseEntity<OrderDTO> markAsPaid(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.markAsPaid(orderNumber));
    }
}
