package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gym_badges")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GymBadge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "image_url")
    private String imageUrl;

    // 關聯到道館 (Many-to-One)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id")
    private Gym gym;

    // 這裡直接存 ID 即可，方便 Service 層直接拿來做 filter 或回傳給前端
    // 如果你有 Chapter Entity，也可以改成 @ManyToOne
    @Column(name = "chapter_id")
    private Long chapterId;

    @Column(name = "journey_id")
    private Long journeyId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 透過 JPA PrePersist 自動寫入建立時間
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}