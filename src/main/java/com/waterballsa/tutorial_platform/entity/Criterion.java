package com.waterballsa.tutorial_platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*; // 引入 Lombok

@Entity
@Table(name = "criteria")
@Data // 自動生成 Getter, Setter, toString, equals, hashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Criterion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String type;
    private Integer requiredQuantity;
    private Long journeyId;
    private Long chapterId;
    private Long gymId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id")
    @JsonIgnore
    @ToString.Exclude // 避免與 Mission 互相 toString 造成無窮迴圈
    private Mission mission;
}