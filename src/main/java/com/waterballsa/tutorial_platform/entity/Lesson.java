package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*; // 務必引入 Lombok
import java.util.List;
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

    @Column(name = "original_id")
    private Long originalId;

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
    @JoinColumn(name = "chapter_id")
    @JsonIgnore
    @ToString.Exclude // 避免無窮迴圈
    private Chapter chapter;

    @Embedded
    @AttributeOverrides({
            // 建議加上這個，避免欄位名稱衝突，且在 DB 中看得很清楚這是獎勵欄位
            @AttributeOverride(name = "exp", column = @Column(name = "reward_exp")),
            @AttributeOverride(name = "coin", column = @Column(name = "reward_coin")),
            @AttributeOverride(name = "subscriptionExtensionInDays", column = @Column(name = "reward_sub_days")),
            @AttributeOverride(name = "externalRewardDescription", column = @Column(name = "reward_desc"))
    })
    private Reward reward;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "lesson_id") // 建立單向關聯 (Skill table 會有 lesson_id)
    private List<Skill> skills;

    @OneToMany(mappedBy = "lesson", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderBy("sortOrder ASC") // 讓取出來的內容自動依照順序排列
    @ToString.Exclude
    private List<LessonContent> contents;

    // ★★★ 新增 1：Lesson 對 Gym 的多對一關聯 (假設一個單元對應一個道館) ★★★
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id") // DB 欄位名稱
    private Gym gym;

    // ★★★ 新增 2：接收 JSON 字串的暫存欄位 (e.g., "5_6") ★★★
    @Transient
    private String relatedGymId;

    @Column(name = "is_core_lesson")
    private boolean isCoreLesson = false; // 預設為 false (弱關聯)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_id") // 這會在資料庫新增 journey_id 欄位
    @JsonIgnore // 通常不需要從 Lesson 反查 Journey 的詳細 JSON，避免迴圈
    private Journey journey;

}