package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

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

    private String name;          // e.g., "行雲流水的設計底層思路"
    private String description;

    private Integer displayOrder; // 排序
    private Integer maxStars;     // 最高星級 (e.g., 1顆星, 3顆星)

    // Gym 通常是掛在 Chapter 下，或者是獨立的 Journey 節點
    // 這裡假設它跟 Chapter 綁定
    @OneToOne
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;

    // 使用者提交的作業紀錄
    @OneToMany(mappedBy = "gym", cascade = CascadeType.ALL)
    private List<GymSubmission> submissions;
}