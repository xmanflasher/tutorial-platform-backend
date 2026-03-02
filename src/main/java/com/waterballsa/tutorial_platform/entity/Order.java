package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String orderNumber;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long journeyId;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    private String paymentMethod; // ATM, CREDIT_CARD, ZINGALA

    @Column(columnDefinition = "TEXT")
    private String paymentDetails; // JSON string for ATM info or Zingala installments

    private String invoiceType; // TAIWAN_ID, MOBILE_CARRIER, NATURAL_PERSON, DONATION
    private String invoiceValue;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime paidAt;

    public enum OrderStatus {
        PENDING, PAID, CANCELLED
    }
}
