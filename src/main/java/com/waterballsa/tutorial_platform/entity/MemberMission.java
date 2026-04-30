package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "member_missions")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MemberMission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // ★ 修正 1：改用 @ManyToOne 關聯物件，不要只存 Long memberId
    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @ToString.Exclude
    private Member member;

    // ★ 修正 2：改用 @ManyToOne 關聯物件，不要只存 Long missionId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id")
    @ToString.Exclude
    private Mission mission;

    @Enumerated(EnumType.STRING)
    private MissionStatus status;

    private Integer currentProgress;
    private LocalDateTime deadline;
    private LocalDateTime completedAt;

    public enum MissionStatus {
        AVAILABLE, LOCKED, IN_PROGRESS, COMPLETED, CLAIMED, FAILED
    }
    @Builder.Default
    @Column(name = "opportunity_cards_used")
    private Integer opportunityCardsUsed = 0; // 預設 0

    // 檢查是否還能使用機會卡 (Max 2)
    public boolean canExtendDeadline() {
        return this.opportunityCardsUsed < 2;
    }
}