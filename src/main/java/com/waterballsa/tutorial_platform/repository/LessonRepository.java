package com.waterballsa.tutorial_platform.repository;

import com.waterballsa.tutorial_platform.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    // 目前不需要額外方法，繼承 JpaRepository 就夠了
}