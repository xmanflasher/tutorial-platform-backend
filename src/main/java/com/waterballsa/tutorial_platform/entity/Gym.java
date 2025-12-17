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

    // ★★★★★ 補上這段：缺少的 reward 欄位 ★★★★★
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "reward_id")
    private Reward reward;

    @OneToMany(mappedBy = "gym", cascade = CascadeType.ALL)
    private List<GymSubmission> submissions;

    @OneToMany(mappedBy = "gym", cascade = CascadeType.ALL)
    private List<Challenge> challenges;
}