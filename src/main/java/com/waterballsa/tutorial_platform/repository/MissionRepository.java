package com.waterballsa.tutorial_platform.repository;

import com.waterballsa.tutorial_platform.entity.Mission;
import com.waterballsa.tutorial_platform.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MissionRepository extends JpaRepository<Mission, Long> {
    // 解決 findAllByJourney_Slug 錯誤
    // 注意：這假設 Mission Entity 裡有一個名為 journey 的欄位
    List<Mission> findAllByJourney_Slug(String slug);
    // 透過原始 ID 找 Lesson
    Optional<Lesson> findByOriginalId(Long originalId);

    // 一次找多個 (給 DataSeeder 用)
    List<Lesson> findByOriginalIdIn(List<Long> originalIds);
}