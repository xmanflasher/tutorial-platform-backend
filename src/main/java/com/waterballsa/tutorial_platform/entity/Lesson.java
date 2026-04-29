package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*; // 務必引入 Lombok
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "lessons")
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Column(name = "original_id")
    @ToString.Include
    private Long originalId;

    // 用來排序單元 (1, 2, 3...)
    @Column(name = "display_order")
    @ToString.Include
    private Integer displayOrder;

    // 控制是否顯示 (true/false)
    @Builder.Default
    @ToString.Include
    private Boolean visible = true;

    @ToString.Include
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ToString.Include
    private String type;
    private String videoLength;
    private Boolean premiumOnly;
    private Boolean passwordRequired;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("lessons")
    @ToString.Exclude // 避免無窮迴圈
    private Chapter chapter;

    @Embedded
    @AttributeOverrides({
            // 建議加上這個，避免欄位名稱衝突，且在 DB 中看得很清楚這是獎議欄位
            @AttributeOverride(name = "exp", column = @Column(name = "reward_exp")),
            @AttributeOverride(name = "coin", column = @Column(name = "reward_coin")),
            @AttributeOverride(name = "subscriptionExtensionInDays", column = @Column(name = "reward_sub_days")),
            @AttributeOverride(name = "externalRewardDescription", column = @Column(name = "reward_desc"))
    })
    private Reward reward;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "lesson_id")
    @Builder.Default
    @ToString.Exclude
    private List<Skill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("sortOrder ASC")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("lesson")
    @ToString.Exclude
    @Builder.Default
    private Set<LessonContent> contents = new LinkedHashSet<>();

    // ★★★ 新增 1：Lesson 對 Gym 的多對一關聯 (假設一個單元對應一個道館) ★★★
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id") // DB 欄位名稱
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("lessons")
    @ToString.Exclude
    private Gym gym;

    // ★★★ 新增 2：接收 JSON 字串的暫存欄位 (e.g., "5_6") ★★★
    @Transient
    private String relatedGymId;

    @Builder.Default
    @Column(name = "is_core_lesson")
    @ToString.Include
    private Boolean isCoreLesson = false; // 預設為 false (弱關聯)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_id") // 這會在資料庫新增 journey_id 欄位
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("lessons")
    @ToString.Exclude
    private Journey journey;

}