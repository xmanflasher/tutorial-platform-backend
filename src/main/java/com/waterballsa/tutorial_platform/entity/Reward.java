package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*; // 引入 Lombok

@Entity
@Table(name = "rewards")
@Data // ★ 自動生成所有 Getter/Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dbId;

    private Integer exp;
    private Integer coin;

    private String externalRewardDescription;

    private Integer subscriptionExtensionInDays;
    private Long journeyId;
}