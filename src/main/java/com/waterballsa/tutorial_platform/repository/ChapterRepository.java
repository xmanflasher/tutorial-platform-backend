package com.waterballsa.tutorial_platform.repository;

import com.waterballsa.tutorial_platform.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
}