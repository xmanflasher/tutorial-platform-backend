package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "chapters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Column(name = "chapter_no")
    private Integer chapterNo;

    // ★ 新增：對應資料庫的 display_order
    @Column(name = "display_order")
    private Integer displayOrder;

    // ★ 新增：對應資料庫的 visible
    // 建議給預設值 true，避免舊資料 null 導致 NullPointerException
    @Column(name = "visible")
    private Boolean visible = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_id")
    @ToString.Exclude
    private Journey journey;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL)
    @OrderBy("id ASC")
    private List<Lesson> lessons;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL)
    private List<Gym> gyms;
}