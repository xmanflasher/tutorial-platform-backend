package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*; // 引入 Lombok
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "journeys")
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Journey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Column(name = "original_id", unique = true, nullable = false)
    @ToString.Include
    private Long originalId;

    @ToString.Include
    private String name;
    
    @ToString.Include
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("journeys")
    @ToString.Exclude
    private Member instructor;

    @Column(name = "visible")
    @Builder.Default
    @ToString.Include
    private Boolean visible = true;

    // 關聯：章節
    @OneToMany(mappedBy = "journey", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 100)
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("journey")
    @ToString.Exclude
    private List<Chapter> chapters = new ArrayList<>();

    // ★★★ 關鍵修正：補回 missions 欄位 ★★★
    @OneToMany(mappedBy = "journey", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 100)
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("journey")
    @ToString.Exclude
    private List<Mission> missions = new ArrayList<>();

    // 關聯：技能
    @OneToMany(mappedBy = "journey", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 100)
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("journey")
    @ToString.Exclude
    private List<Skill> skills = new ArrayList<>();

    // ★★★ [新增] 道館關聯 (對應 Gym.java 的 journey 屬性) ★★★
    @OneToMany(mappedBy = "journey", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 100)
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("journey")
    @ToString.Exclude
    private List<Gym> gyms = new ArrayList<>();

    // 關聯：選單
    @OneToMany(mappedBy = "journey", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @BatchSize(size = 100)
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("journey")
    @ToString.Exclude
    private List<JourneyMenu> menus = new ArrayList<>();

    // 關聯：徽章 (SD-10)
    @Builder.Default
    @Transient // 先設為 Transient 方便 JSON 反序列化，再手動存入 db
    @ToString.Exclude
    private List<GymBadge> badges = new ArrayList<>();
}