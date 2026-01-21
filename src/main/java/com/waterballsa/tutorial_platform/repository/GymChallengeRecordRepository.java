package com.waterballsa.tutorial_platform.repository;

import com.waterballsa.tutorial_platform.entity.GymChallengeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GymChallengeRecordRepository extends JpaRepository<GymChallengeRecord, Long> {
    // 根據 userId 查詢，並依建立時間倒序排列
    List<GymChallengeRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
}