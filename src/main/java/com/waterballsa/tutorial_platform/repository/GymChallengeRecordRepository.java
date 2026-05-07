package com.waterballsa.tutorial_platform.repository;

import com.waterballsa.tutorial_platform.entity.GymChallengeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GymChallengeRecordRepository extends JpaRepository<GymChallengeRecord, Long> {
    List<GymChallengeRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<GymChallengeRecord> findByGymIdAndUserIdOrderByCreatedAtDesc(Long gymId, Long userId);
    List<GymChallengeRecord> findByGymIdAndGymChallengeIdAndUserIdOrderByCreatedAtDesc(Long gymId, Long gymChallengeId, Long userId);
    List<GymChallengeRecord> findByUserIdAndStatus(Long userId, GymChallengeRecord.ChallengeStatus status);
}