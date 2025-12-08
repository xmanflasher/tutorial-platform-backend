package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*; // 引入 Lombok
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "lessons")
@Data // ★ 自動生成 Getter/Setter (這就是為什麼找不到 getReward 的原因)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // ★ 新增：控制排序
    @Column(name = "display_order")
    private Integer displayOrder;

    // ★ 新增：控制顯示 (預設 true)
    @Builder.Default
    private Boolean visible = true;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String type;
    private String videoLength;
    private Boolean premiumOnly;
    private Boolean passwordRequired;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_db_id")
    @JsonIgnore
    private Chapter chapter;

    // Lesson 也有 Reward (一對一)
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "reward_id")
    private Reward reward;
}