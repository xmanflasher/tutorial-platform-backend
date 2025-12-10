package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "member_missions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberMission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "mission_id")
    private Mission mission;

    @Enumerated(EnumType.STRING)
    private MissionStatus status;

    private Integer currentProgress;
    private LocalDateTime deadline;
    private LocalDateTime completedAt;

    public enum MissionStatus {
        AVAILABLE,   // 可接取 (未接)
        LOCKED,      // 未達標 (不能接)
        IN_PROGRESS, // 進行中
        COMPLETED,   // 已完成 (未領獎)
        CLAIMED,     // 已領獎
        FAILED       // 失敗/過期
    }
}