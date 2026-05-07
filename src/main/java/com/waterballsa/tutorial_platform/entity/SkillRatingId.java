package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillRatingId implements Serializable {
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "journey_id")
    private Long journeyId;
}
