package com.waterballsa.tutorial_platform.repository;

import com.waterballsa.tutorial_platform.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    // 透過原始 ID 找 Lesson
    Optional<Lesson> findByOriginalId(Long originalId);

    // 一次找多個 (舊方法，可以保留給其他用途)
    List<Lesson> findByOriginalIdIn(List<Long> originalIds);

    // ★★★ 新增：一次找多個，且限定必須屬於特定 Journey ★★★
    // 這會自動生成 SQL: SELECT ... FROM lessons l JOIN chapters c ON ... JOIN journeys j ON ... WHERE l.original_id IN (?) AND j.id = ?
    List<Lesson> findByOriginalIdInAndChapter_Journey_Id(List<Long> originalIds, Long journeyId);
}