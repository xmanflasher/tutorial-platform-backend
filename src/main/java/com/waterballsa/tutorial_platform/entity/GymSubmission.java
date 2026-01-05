package com.waterballsa.tutorial_platform.entity;

import com.waterballsa.tutorial_platform.converter.JsonToMapConverter;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "gym_submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GymSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "gym_id")
    private Gym gym;

    @Column(name = "challenge_id")
    private Long challengeId;

    // ★★★ 修正這裡：完全對照 API JSON 的值 ★★★
    @Enumerated(EnumType.STRING)
    private SubmissionStatus status;

    @Column(name = "files_json", columnDefinition = "jsonb")
    @Convert(converter = JsonToMapConverter.class)
    private Map<String, String> submissionFiles;

    @Column(columnDefinition = "jsonb")
    @Convert(converter = JsonToMapConverter.class)
    private Map<String, String> ratings;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    private Integer grade;
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