package com.waterballsa.tutorial_platform.repository;

import com.waterballsa.tutorial_platform.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    Optional<Announcement> findFirstByUserIdOrUserIdIsNullOrderByCreatedAtDesc(Long userId);
    
    List<Announcement> findByUserIdOrUserIdIsNullOrderByCreatedAtDesc(Long userId);

    Optional<Announcement> findFirstByOrderByCreatedAtDesc();
}
