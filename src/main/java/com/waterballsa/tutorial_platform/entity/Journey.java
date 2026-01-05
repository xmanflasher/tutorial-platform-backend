package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*; // 引入 Lombok
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "journeys")
@Data // 自動生成 Getter/Setter (包含 getMissions)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Journey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_id")
    private String originalId;

    private String name;
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "visible")
    @Builder.Default
    private Boolean visible = true;

    // 關聯：章節
    @OneToMany(mappedBy = "journey", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Chapter> chapters = new ArrayList<>();

    // ★★★ 關鍵修正：補回 missions 欄位 ★★★
    @OneToMany(mappedBy = "journey", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Mission> missions = new ArrayList<>();

    // 關聯：技能
    @OneToMany(mappedBy = "journey", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Skill> skills = new ArrayList<>();

    // 關聯：選單
    @OneToMany(mappedBy = "journey", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<JourneyMenu> menus = new ArrayList<>();
}