package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.ArrayList;
import com.waterballsa.tutorial_platform.enums.GymType;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "gyms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gym {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_id")
    private Long originalId;

    @Column(name = "code")
    private String code;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "display_order")
    private Integer displayOrder;

    private Integer difficulty;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private GymType type;

    private Integer maxStars;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_id")
    @ToString.Exclude
    private Journey journey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    @ToString.Exclude
    private Chapter chapter;

    @Embedded
    @AttributeOverrides({
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

    // =================================================================
    // ★★★ [核心修正] 改為 @ManyToMany 以建立獨立中間表 ★★★
    // =================================================================
    //@ManyToMany(fetch = FetchType.LAZY)
    //@JoinTable(
//            name = "gym_lessons", // 產生的中間表名稱
//            joinColumns = @JoinColumn(name = "gym_id"),
//            inverseJoinColumns = @JoinColumn(name = "lesson_id")
//    )
    @Builder.Default
    @ToString.Exclude // 避免循環引用導致 StackOverflow
    @OneToMany(mappedBy = "gym", fetch = FetchType.LAZY)// 意思：去 Lesson 類別找一個叫做 "gym" 的屬性，以它為主
    private List<Lesson> relatedLessons = new ArrayList<>();

    // 用於接收 JSON 資料，不存入資料庫欄位
    @Transient
    @JsonProperty("relatedLessonIds")
    @Builder.Default
    private List<String> relatedLessonIds = new ArrayList<>();

    // =================================================================
    // ★★★ Helper Methods ★★★
    // =================================================================
    @JsonProperty("rewardExp")
    public Integer getRewardExp() {
        return (reward != null && reward.getExp() != null) ? reward.getExp() : 0;
    }

    public Integer getRewardCoin() {
        return (reward != null && reward.getCoin() != null) ? reward.getCoin() : 0;
    }

    /**
     * 加入課程的 Helper Method，確保不重複加入
     */
    public void addRelatedLesson(Lesson lesson) {
        if (this.relatedLessons == null) {
            this.relatedLessons = new ArrayList<>();
        }
        if (!this.relatedLessons.contains(lesson)) {
            this.relatedLessons.add(lesson);
        }
    }
}