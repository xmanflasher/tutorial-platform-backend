package com.waterballsa.tutorial_platform.entity;

import com.fasterxml.jackson.annotation.JsonAnySetter; // ★ 要加這個
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "mission_requirements")
@Data
public class MissionRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_id")
    private String originalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id")
    @JsonIgnore // 避免循環引用
    @ToString.Exclude
    private Mission mission;

    // 我們會在 Seeder 裡手動設定這個值 ('PREREQUISITE' 或 'CRITERIA')
    @Column(nullable = false)
    private String category;

    @JsonProperty("type")
    @Column(name = "condition_type", nullable = false)
    private String conditionType;

    private String name;
    private String description;

    @Column(name = "required_quantity")
    private Integer requiredQuantity;

    // ★★★ 魔法修正：讓 Jackson 自動把 gymId, chapterId 等未知欄位塞進來 ★★★
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> params = new HashMap<>();

    // 這個方法告訴 Jackson：「看到你認不得的欄位 (如 gymId)，就塞進 params Map 裡」
    @JsonAnySetter
    public void addParam(String key, Object value) {
        this.params.put(key, value);
    }
}