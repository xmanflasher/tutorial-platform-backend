package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*; // 引入 Lombok
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "skills")
@Data // ★ 自動生成 Getter, Setter, toString, equals, hashCode
@NoArgsConstructor // ★ 自動生成無參數建構子 (JPA 需要)
@AllArgsConstructor // ★ 自動生成全參數建構子
@Builder // ★ 自動生成 Builder 模式
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_id")
    private Long originalId;

    private String name;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_id")
    @JsonIgnore
    private Journey journey;
}