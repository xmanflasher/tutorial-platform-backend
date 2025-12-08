package com.waterballsa.tutorial_platform.repository;

import com.waterballsa.tutorial_platform.entity.Journey;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JourneyRepository extends JpaRepository<Journey, Long> {
    // 必須要有這行，JPA 才會自動生成用 slug 查詢的 SQL
    Optional<Journey> findBySlug(String slug);
}