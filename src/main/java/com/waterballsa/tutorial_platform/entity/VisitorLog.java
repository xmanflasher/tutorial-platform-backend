package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "visitor_logs")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class VisitorLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String visitorId;
    private String category; // GUEST, PASSERBY
    private LocalDateTime createdAt;
    private Long linkedMemberId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
