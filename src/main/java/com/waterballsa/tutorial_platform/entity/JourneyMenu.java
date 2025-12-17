package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "journey_menus")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JourneyMenu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String icon; // 對應前端 Icon Map key
    private String href;
    private Integer displayOrder;
    private Boolean visible;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_id")
    @ToString.Exclude
    private Journey journey;
}