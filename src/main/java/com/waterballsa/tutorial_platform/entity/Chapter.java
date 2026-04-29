package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import org.hibernate.annotations.BatchSize;
import java.util.LinkedHashSet;


@Entity
@Table(name = "chapters")
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Column(name = "original_id")
    @ToString.Include
    private Long originalId;

    @ToString.Include
    private String name;
    private String description;

    // ★ 新增：對應資料庫的 display_order
    @Column(name = "display_order")
    @ToString.Include
    private Integer displayOrder;

    // ★ 新增：對應資料庫的 visible
    // 建議給預設值 true，避免舊資料 null 導致 NullPointerException
    @Builder.Default
    @Column(name = "visible")
    @ToString.Include
    private Boolean visible = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("chapters")
    @ToString.Exclude
    @JsonIgnore
    private Journey journey;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL)
    @OrderBy("id ASC")
    @BatchSize(size = 100)
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("chapter")
    @ToString.Exclude
    private List<Lesson> lessons = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL)
    @BatchSize(size = 100)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("chapter")
    @ToString.Exclude
    private List<Gym> gyms = new ArrayList<>();

    @Embedded
    @AttributeOverrides({
            // 建議加上這個，避免欄位名稱衝突，且在 DB 中看得很清楚這是獎勵欄位
            @AttributeOverride(name = "exp", column = @Column(name = "reward_exp")),
            @AttributeOverride(name = "coin", column = @Column(name = "reward_coin")),
            @AttributeOverride(name = "subscriptionExtensionInDays", column = @Column(name = "reward_sub_days")),
            @AttributeOverride(name = "externalRewardDescription", column = @Column(name = "reward_desc"))
    })
    private Reward reward;

}