package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.List;
import com.waterballsa.tutorial_platform.entity.vo.SubmissionFieldConfig;
import com.waterballsa.tutorial_platform.enums.ChallengeType;

@Entity
@Table(name = "challenges")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_id")
    private Long originalId;

    private String name;  // 例如："Showdown ! 撲克牌遊戲建模 ★"

    @Enumerated(EnumType.STRING)
    private ChallengeType type;

    // ★ 新增：建議完成天數
    @Column(name = "recommend_duration")
    private Integer recommendDurationInDays;

    // ★ 新增：最大完成天數
    @Column(name = "max_duration")
    private Integer maxDurationInDays;

    // ★ 新增：繳交檔案設定 (存成 JSONB)
    // 這裡會直接把 List<SubmissionFieldConfig> 轉成 JSON 字串存入資料庫
    @Column(name = "submission_fields", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<SubmissionFieldConfig> submissionFields;

    // 雙向關聯：對應 Gym.java 裡面的 List<Challenge>
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id")
    @ToString.Exclude
    private Gym gym;
}