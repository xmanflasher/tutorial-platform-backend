package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "challenges")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;  // 例如："Showdown ! 撲克牌遊戲建模 ★"

    private String type;  // 例如："PRACTICAL_CHALLENGE" (實作), "INSTANT_CHALLENGE" (即時)

    // 這裡可以根據 JSON 需求增加更多欄位，例如 description, recommendDurationInDays 等

    // ★ 雙向關聯：對應 Gym.java 裡面的 List<Challenge>
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id")
    @ToString.Exclude
    private Gym gym;
}