package com.waterballsa.tutorial_platform.repository;

import com.waterballsa.tutorial_platform.entity.MemberMission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MemberMissionRepository extends JpaRepository<MemberMission, Long> {

    // 查詢某會員的所有任務狀態
    List<MemberMission> findByMemberId(Long memberId);
}