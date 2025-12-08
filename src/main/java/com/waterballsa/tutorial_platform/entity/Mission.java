package com.waterballsa.tutorial_platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*; // 引入 Lombok
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "missions")
@Data // 自動生成 Getter, Setter, toString, equals, hashCode
@NoArgsConstructor // JPA 必要
@AllArgsConstructor // Builder 必要
@Builder // 方便建構物件
public class Mission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer durationInDays;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_db_id")
    @JsonIgnore
    @ToString.Exclude // ★ 重要：避免與 Journey 互相呼叫 toString 造成無窮迴圈
    @EqualsAndHashCode.Exclude // ★ 重要：避免 equals/hashCode 造成無窮迴圈
    private Journey journey;

    // 關聯: Reward (一對一)
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "reward_id")
    private Reward reward;

    // 關聯: Prerequisites (一對多)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "mission")
    @Builder.Default // 確保使用 Builder 建立時，若沒給值會預設為 ArrayList 而不是 null
    private List<Prerequisite> prerequisites = new ArrayList<>();

    // 關聯: Criteria (一對多)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "mission")
    @Builder.Default
    private List<Criterion> criteria = new ArrayList<>();
}