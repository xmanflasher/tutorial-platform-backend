package com.waterballsa.tutorial_platform.repository;

import com.waterballsa.tutorial_platform.entity.MemberBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MemberBadgeRepository extends JpaRepository<MemberBadge, Long> {
    List<MemberBadge> findByMemberId(Long memberId);
}
