package com.waterballsa.tutorial_platform.repository;

import com.waterballsa.tutorial_platform.entity.GymSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GymSubmissionRepository extends JpaRepository<GymSubmission, Long> {

    // 查詢某個會員的所有繳交紀錄 (用來判斷哪些關卡已解鎖/已通過)
    List<GymSubmission> findByMemberId(Long memberId);
    // 用於檢查道館是否通過
    boolean existsByMemberIdAndGymIdAndStatus(Long memberId, Long gymId, String status);
}