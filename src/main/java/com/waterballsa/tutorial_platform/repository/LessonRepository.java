package com.waterballsa.tutorial_platform.repository;

import com.waterballsa.tutorial_platform.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    // 透過原始 ID 找 Lesson
    Optional<Lesson> findByOriginalId(String originalId);

    // 一次找多個 (給 DataSeeder 用)
    List<Lesson> findByOriginalIdIn(List<String> originalIds);

}