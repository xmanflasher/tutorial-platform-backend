package com.waterballsa.tutorial_platform.repository;

import com.waterballsa.tutorial_platform.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    List<Challenge> findByGymId(Long gymId);
}
