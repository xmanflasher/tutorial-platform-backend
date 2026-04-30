package com.waterballsa.tutorial_platform.entity;

import com.waterballsa.tutorial_platform.converter.JsonToMapConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "gym_submissions")
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GymSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("submissions")
    @ToString.Exclude
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("submissions")
    @ToString.Exclude
    private Gym gym;

    @Column(name = "challenge_id")
    @ToString.Include
    private Long challengeId;

    // ★★★ 修正這裡：完全對照 API JSON 的值 ★★★
    @Enumerated(EnumType.STRING)
    @ToString.Include
    private SubmissionStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "files_json", columnDefinition = "jsonb")
    private Map<String, String> submissionFiles;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> ratings;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @ToString.Include
    private Integer grade;
    @ToString.Include
    private LocalDateTime submittedAt;
    private LocalDateTime bookingCompletedAt;
    private LocalDateTime reviewedAt;

    public enum SubmissionStatus {
        // 對應前端的 "未提交"
        UNSUBMITTED,

        // 對應前端 "已提交" (submission: {...})
        SUBMITTED,

        // 修正：對應 JSON 中的 "SUCCESS"
        SUCCESS,

        // 修正：對應 JSON 中的 "FAILED"
        FAILED
    }
}