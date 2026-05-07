package com.waterballsa.tutorial_platform.repository;

import com.waterballsa.tutorial_platform.entity.SkillRating;
import com.waterballsa.tutorial_platform.entity.SkillRatingId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRatingRepository extends JpaRepository<SkillRating, SkillRatingId> {
}
