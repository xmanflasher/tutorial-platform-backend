package com.waterballsa.tutorial_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private String orderNumber;
    private Long userId;
    private Long journeyId;
    private String journeyName;
    private String journeySlug;
    private Long amount;
    private String status;
    private String paymentMethod;
    private String paymentDetails;
    private String invoiceType;
    private String invoiceValue;
    private Long createdAt;
    private Long paidAt;
}
