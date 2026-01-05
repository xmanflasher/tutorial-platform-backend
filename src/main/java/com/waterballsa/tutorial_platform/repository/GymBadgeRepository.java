package com.waterballsa.tutorial_platform.repository;

import com.waterballsa.tutorial_platform.entity.GymBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GymBadgeRepository extends JpaRepository<GymBadge, Long> {
    List<GymBadge> findByJourneyId(Long journeyId);
}