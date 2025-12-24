package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable // 標記為可嵌入物件
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reward {
    // 不需要 ID 了，因為它現在只是 Mission 資料表的一部分

    private Integer exp;
    private Integer coin;
    private Integer subscriptionExtensionInDays;
    private String externalRewardDescription;
}