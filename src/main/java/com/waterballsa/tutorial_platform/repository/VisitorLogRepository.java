package com.waterballsa.tutorial_platform.repository;

import com.waterballsa.tutorial_platform.entity.VisitorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VisitorLogRepository extends JpaRepository<VisitorLog, Long> {
    Optional<VisitorLog> findByVisitorId(String visitorId);
}
