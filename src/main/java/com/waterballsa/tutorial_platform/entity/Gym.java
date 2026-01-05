package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "gyms")
@Data // ★ 確保有這個，才有 getReward()
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gym {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_id")
    private String originalId;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer displayOrder;
    private Integer maxStars;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    @ToString.Exclude
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

    @OneToMany(mappedBy = "gym", cascade = CascadeType.ALL)
    private List<GymSubmission> submissions;

    @OneToMany(mappedBy = "gym", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Challenge> challenges = new ArrayList<>();

    // ★★★ 1. 真實的資料庫關聯 (存 Lesson 物件) ★★★
    // 這裡使用 @ManyToMany 是因為一個 Lesson 可能被多個 Gym 關聯，
    // 或者一個 Gym 關聯多個 Lesson。如果是一對多，可以用 @OneToMany。
    // 為了簡單起見，這裡建立一個中間表 gyms_lessons
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "gym_related_lessons",
            joinColumns = @JoinColumn(name = "gym_id"),
            inverseJoinColumns = @JoinColumn(name = "lesson_id")
    )
    @Builder.Default
    private List<Lesson> relatedLessons = new ArrayList<>();

    // ★★★ 2. 暫存欄位 (接收 JSON 裡的 ID 列表) ★★★
    // @Transient 代表這個欄位「不」對應資料庫的任何欄位
    // Jackson (JSON parser) 會寫入它，但 Hibernate (JPA) 會忽略它
    @Transient
    private List<String> relatedLessonIds;
}