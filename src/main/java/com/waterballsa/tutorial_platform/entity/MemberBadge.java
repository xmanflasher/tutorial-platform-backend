package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "member_badges")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberBadge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "badge_id", nullable = false)
    private Long badgeId;

    @Column(name = "awarded_at")
    private LocalDateTime awardedAt;

    @Column(name = "is_shown", nullable = false)
    @Builder.Default
    private Boolean isShown = false; // 前端是否已播放過獲得動畫

    @PrePersist
    public void prePersist() {
        if (this.awardedAt == null) {
            this.awardedAt = LocalDateTime.now();
        }
    }
}
