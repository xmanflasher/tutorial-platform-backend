package com.waterballsa.tutorial_platform.repository;

import com.waterballsa.tutorial_platform.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    Optional<Chapter> findByOriginalId(Long originalId);
}