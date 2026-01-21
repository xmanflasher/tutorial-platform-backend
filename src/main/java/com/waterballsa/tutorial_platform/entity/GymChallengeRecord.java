package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "gym_challenge_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GymChallengeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "journey_id")
    private Long journeyId;

    @Column(name = "chapter_id")
    private Long chapterId;

    @Column(name = "gym_id", nullable = false)
    private Long gymId;

    @Column(name = "gym_challenge_id", nullable = false)
    private Long gymChallengeId;

    @Enumerated(EnumType.STRING)
    private ChallengeStatus status; // SUCCESS, FAILED, SUBMITTED

    @Column(columnDefinition = "TEXT")
    private String feedback;

    // ★★★ 關鍵：儲存 Ratings Map (例如 "1": "SS", "2": "A")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, String> ratings = new HashMap<>();

    // ★★★ 關鍵：儲存 Submission 連結 (ood_uml, code_files 等)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, String> submission = new HashMap<>();

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "booking_completed_at")
    private Date bookingCompletedAt;

    @Column(name = "completed_at")
    private Date completedAt;

    @Column(name = "reviewed_at")
    private Date reviewedAt;

    public enum ChallengeStatus {
        SUCCESS, FAILED, SUBMITTED
    }
}