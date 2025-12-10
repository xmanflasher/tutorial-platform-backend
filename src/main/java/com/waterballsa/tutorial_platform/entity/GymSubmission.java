package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

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

    // 狀態：UNSUBMITTED, SUBMITTED, REJECTED, PASSED
    @Enumerated(EnumType.STRING)
    private SubmissionStatus status;

    // 繳交的檔案連結 (JSON 格式存多個檔案連結，或是開另一張表)
    // 簡單起見先存 String
    @Column(columnDefinition = "TEXT")
    private String filesJson;

    private Integer grade; // 獲得星數
    private LocalDateTime submittedAt;

    public enum SubmissionStatus {
        UNSUBMITTED, SUBMITTED, UNDER_REVIEW, PASSED, REJECTED
    }
}