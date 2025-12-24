package com.waterballsa.tutorial_platform.repository;

import com.waterballsa.tutorial_platform.entity.MemberMission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberMissionRepository extends JpaRepository<MemberMission, Long> {
    // 查詢某會員的所有紀錄
    List<MemberMission> findAllByMember_Id(Long memberId);

    // ★★★ 關鍵修正：確保這裡有定義這個方法，且名稱包含底線 ★★★
    Optional<MemberMission> findByMember_IdAndMission_Id(Long memberId, Long missionId);

    // 1. 一次查詢多個任務狀態 (解決 findByMemberIdAndMissionIdIn)
    // 注意：方法名稱中的 MemberId 對應 Entity 裡的 member.id，MissionId 對應 mission.id
    List<MemberMission> findByMemberIdAndMissionIdIn(Long memberId, List<Long> missionIds);

    // 2. 檢查是否有特定狀態的任務 (解決 existsByMemberIdAndStatus)
    boolean existsByMemberIdAndStatus(Long memberId, MemberMission.MissionStatus status);

    // 補上 Service 裡用到的單一查詢 (如果你原本是用 findByMember_IdAndMission_Id 也可以，但建議統一命名風格)
    Optional<MemberMission> findByMemberIdAndMissionId(Long memberId, Long missionId);
}