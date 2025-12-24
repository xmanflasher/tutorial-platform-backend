package com.waterballsa.tutorial_platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where; // ★ 引入這個

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

    private Integer durationInDays;

    @Column(name = "display_order")
    private Integer displayOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_id")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Journey journey;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "exp", column = @Column(name = "reward_exp")),
            @AttributeOverride(name = "coin", column = @Column(name = "reward_coin")),
            @AttributeOverride(name = "subscriptionExtensionInDays", column = @Column(name = "reward_sub_days")),
            @AttributeOverride(name = "externalRewardDescription", column = @Column(name = "external_reward_desc"))
    })
    private Reward reward;

    // ★ 1. 前置條件列表
    // 加入 @Where 讓 Hibernate 撈資料時只撈 category = 'PREREQUISITE' 的項目
    @Builder.Default
    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Where(clause = "category = 'PREREQUISITE'")
    @JsonProperty("prerequisites")
    private List<MissionRequirement> prerequisites = new ArrayList<>();

    // ★ 2. 驗收條件列表
    // 加入 @Where 讓 Hibernate 撈資料時只撈 category = 'CRITERIA' 的項目
    @Builder.Default
    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Where(clause = "category = 'CRITERIA'") // 假設資料庫存的是 "CRITERIA" (如果存 "COMPLETION" 請自行修改)
    @JsonProperty("criteria")
    private List<MissionRequirement> criteria = new ArrayList<>();

    /**
     * ★★★ 解決編譯錯誤的關鍵方法 ★★★
     * Service 層習慣呼叫 getRequirements() 來一次取得所有條件並進行 filter。
     * 這裡我們手動合併兩個 list 回傳給 Service 使用。
     */
    public List<MissionRequirement> getRequirements() {
        List<MissionRequirement> all = new ArrayList<>();
        if (this.prerequisites != null) {
            all.addAll(this.prerequisites);
        }
        if (this.criteria != null) {
            all.addAll(this.criteria);
        }
        return all;
    }

    // ★ 選用：如果你有在後端建立 Mission 的邏輯，建議加上這個 Helper
    // 確保加入列表時，category 欄位會被正確設定
    public void addPrerequisite(MissionRequirement req) {
        req.setCategory("PREREQUISITE");
        req.setMission(this);
        this.prerequisites.add(req);
    }

    public void addCriterion(MissionRequirement req) {
        req.setCategory("CRITERIA"); // 或是 "COMPLETION"，需與 Service 字串一致
        req.setMission(this);
        this.criteria.add(req);
    }
}