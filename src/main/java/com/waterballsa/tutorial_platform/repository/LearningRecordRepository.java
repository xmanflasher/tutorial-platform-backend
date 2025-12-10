package com.waterballsa.tutorial_platform.repository;

import com.waterballsa.tutorial_platform.entity.LearningRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningRecordRepository extends JpaRepository<LearningRecord, Long> {

    // 檢查某人是否看過某單元 (回傳 true/false)
    boolean existsByMemberIdAndLessonId(Long memberId, Long lessonId);
}