package com.waterballsa.tutorial_platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "missions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // ★★★ 修正這裡：統一變數名稱為 durationDays (配合 DataSeeder) ★★★
    private Integer durationDays;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_db_id")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Journey journey;

    // 舊有的 Reward 關聯 (JSON 匯入時會用到)
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "reward_id")
    private Reward reward;

    // 關聯清單 (JSON 匯入時會用到)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "mission", orphanRemoval = true)
    @Builder.Default
    private List<Prerequisite> prerequisites = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "mission", orphanRemoval = true)
    @Builder.Default
    private List<Criterion> criteria = new ArrayList<>();

    // --- 新增的任務系統欄位 (MissionService 用) ---

    // 開放條件 (例如 "gym_pass:3")
    private String unlockCondition;

    // 完成條件字串 (例如 "gym_pass:4")
    private String completionCriteria;

    @Enumerated(EnumType.STRING)
    private RewardType rewardType;

    private Integer rewardValue;

    public enum RewardType { SUBSCRIPTION, ITEM, EXP, COIN }
}