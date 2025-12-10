package com.waterballsa.tutorial_platform.repository;

import com.waterballsa.tutorial_platform.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRepository extends JpaRepository<Mission, Long> {
    // 通常使用 findAll() 即可，若有篩選需求可加方法
}