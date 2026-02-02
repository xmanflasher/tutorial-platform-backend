package com.waterballsa.tutorial_platform.repository;

import com.waterballsa.tutorial_platform.entity.MissionRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RequirementRepository extends JpaRepository<MissionRequirement, Long> {
    // 這是在 DataSeeder 裡用到的查詢方法
    List<MissionRequirement> findByMissionId(Long missionId);
}