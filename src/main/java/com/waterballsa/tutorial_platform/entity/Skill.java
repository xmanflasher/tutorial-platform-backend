package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*; // 引入 Lombok
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "skills")
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill {
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("skills")
    @ToString.Exclude
    private Journey journey;
}