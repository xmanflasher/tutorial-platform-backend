package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

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

    // ★ 新增：控制排序
    @Column(name = "display_order")
    private Integer displayOrder;

    // ★ 新增：控制顯示 (預設 true)
    @Builder.Default
    private Boolean visible = true;

    // ★★★ 這就是 Key！對應到資料庫的 journey_db_id ★★★
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_db_id") // 指定外鍵欄位名稱
    @JsonIgnore // 防止 JSON 遞迴死循環
    private Journey journey;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lesson> lessons;
}