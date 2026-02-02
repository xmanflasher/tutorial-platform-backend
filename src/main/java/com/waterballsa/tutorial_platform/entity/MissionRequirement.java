package com.waterballsa.tutorial_platform.entity;

import com.fasterxml.jackson.annotation.JsonAnySetter;
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
    private Long originalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id")
    @JsonIgnore
    @ToString.Exclude
    private Mission mission;

    @Column(nullable = false)
    private String category;

    @JsonProperty("type")
    @Column(name = "condition_type", nullable = false)
    private String conditionType;

    private String name;
    private String description;

    @Column(name = "required_quantity")
    private Integer requiredQuantity;

    // ------------------------------------------------------------------
    // ★★★ 新增：解析後的真實 DB ID (給後端邏輯快速查詢用) ★★★
    // ------------------------------------------------------------------

    // 如果條件是 GYM_CHALLENGE_SUCCESS，Seeder 會填入這裡
    @Column(name = "target_gym_id")
    private Long targetGymId;

    // 如果條件是 MISSION_COMPLETED，Seeder 會填入這裡
    @Column(name = "target_mission_id")
    private Long targetMissionId;

    // ------------------------------------------------------------------
    // 下面這區維持原樣：保留原始 JSON 參數 (給前端顯示或是除錯用)
    // ------------------------------------------------------------------
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> params = new HashMap<>();

    @JsonAnySetter
    public void addParam(String key, Object value) {
        this.params.put(key, value);
    }
}