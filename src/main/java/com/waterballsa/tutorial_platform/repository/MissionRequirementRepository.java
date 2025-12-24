package com.waterballsa.tutorial_platform.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.waterballsa.tutorial_platform.entity.MissionRequirement;

public interface MissionRequirementRepository extends JpaRepository<MissionRequirement, Long> {
    // 基本上不需要寫什麼，除非你要單獨撈條件
}