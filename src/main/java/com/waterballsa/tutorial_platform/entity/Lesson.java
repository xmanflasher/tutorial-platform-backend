package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*; // 務必引入 Lombok
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "lessons")
@Data // ★★★ 救星：自動產生 getReward(), getName(), getDescription() 等方法
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 用來排序單元 (1, 2, 3...)
    @Column(name = "display_order")
    private Integer displayOrder;

    // 控制是否顯示 (true/false)
    @Builder.Default
    private Boolean visible = true;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String type;
    private String videoLength;
    private Boolean premiumOnly;
    private Boolean passwordRequired;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_db_id")
    @JsonIgnore
    @ToString.Exclude // 避免無窮迴圈
    private Chapter chapter;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "reward_id")
    private Reward reward;
}