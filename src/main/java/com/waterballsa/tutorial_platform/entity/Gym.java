package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

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

    @OneToMany(mappedBy = "gym", cascade = CascadeType.ALL)
    private List<Challenge> challenges;
}