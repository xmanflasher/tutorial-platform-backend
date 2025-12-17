package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rewards")
@Data // ★★★ 關鍵：這會自動產生 setId() 方法
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ★ 必須要有 ID，DataSeeder 才能呼叫 setId(null)

    private Integer exp;
    private Integer coin;

    // 根據你的 JSON，可能還有這兩個欄位
    private Integer subscriptionExtensionInDays;
    private String externalRewardDescription;
}