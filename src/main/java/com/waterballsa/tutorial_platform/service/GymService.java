package com.waterballsa.tutorial_platform.service;

import com.waterballsa.tutorial_platform.dto.ChallengeRecordDTO;
import com.waterballsa.tutorial_platform.dto.GymBadgeDTO;
import com.waterballsa.tutorial_platform.dto.GymStatusDTO;
import com.waterballsa.tutorial_platform.entity.Gym;
import com.waterballsa.tutorial_platform.entity.GymBadge;
import com.waterballsa.tutorial_platform.entity.GymSubmission;
import com.waterballsa.tutorial_platform.repository.GymBadgeRepository;
import com.waterballsa.tutorial_platform.repository.GymRepository;
import com.waterballsa.tutorial_platform.repository.GymSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GymService {

    private final GymRepository gymRepository;
    private final GymSubmissionRepository submissionRepository;
    private final GymBadgeRepository gymBadgeRepository; // 記得建立這個 Repository

    // --- 功能 1: 取得道館地圖狀態 (主線/支線解鎖邏輯) ---
    public List<GymStatusDTO> getGymMap(Long memberId) {
        // 1. 撈出所有 Gym (依照順序)
        List<Gym> allGyms = gymRepository.findAllByOrderByDisplayOrderAsc();

        // 2. 撈出該使用者的所有提交紀錄
        List<GymSubmission> submissions = submissionRepository.findByMemberId(memberId);

        List<GymStatusDTO> result = new ArrayList<>();
        boolean isPreviousPassed = true; // 第一關預設解鎖

        for (Gym gym : allGyms) {
            // 找找看有沒有這個 Gym 的提交紀錄 (取最新的一筆，或判斷是否有 SUCCESS)
            // 這裡假設一個 Gym 可能有多筆 submission，我們要找有沒有成功的
            boolean hasPassed = submissions.stream()
                    .anyMatch(s -> s.getGym().getId().equals(gym.getId())
                            && s.getStatus() == GymSubmission.SubmissionStatus.SUCCESS);

            // 取得該 Gym 最高分的紀錄 (用來顯示星數)
            Integer maxGrade = submissions.stream()
                    .filter(s -> s.getGym().getId().equals(gym.getId()))
                    .map(GymSubmission::getGrade)
                    .filter(g -> g != null)
                    .max(Integer::compareTo)
                    .orElse(0);

            String status = "LOCKED";

            if (hasPassed) {
                status = "PASSED"; // 前端可能叫 "CLEARED" 或 "SUCCESS"，請對照你的 UI 邏輯
                isPreviousPassed = true;
            } else if (isPreviousPassed) {
                status = "OPEN"; // 上一關過了，這關才開啟
                isPreviousPassed = false; // 這關還沒過，下一關先鎖住
            }

            result.add(GymStatusDTO.builder()
                    .gymId(gym.getId())
                    .name(gym.getName())
                    .status(status) // LOCKED, OPEN, PASSED
                    .stars(maxGrade)
                    .build());
        }
        return result;
    }

    // --- 功能 2: 取得使用者挑戰歷程 (對應挑戰歷程頁面) ---
    public List<ChallengeRecordDTO> getUserChallengeRecords(Long userId) {
        List<GymSubmission> submissions = submissionRepository.findByMemberId(userId);

        // 依照提交時間倒序排列 (新的在前)
        return submissions.stream()
                .sorted((a, b) -> {
                    LocalDateTime t1 = a.getSubmittedAt() != null ? a.getSubmittedAt() : LocalDateTime.MIN;
                    LocalDateTime t2 = b.getSubmittedAt() != null ? b.getSubmittedAt() : LocalDateTime.MIN;
                    return t2.compareTo(t1);
                })
                .map(this::toChallengeRecordDTO)
                .collect(Collectors.toList());
    }

    // ★ 修改方法簽名，接收 userId
    public List<GymBadgeDTO> getBadgesByJourney(Long userId, Long journeyId) {
        // 1. 從 DB 撈出該旅程的所有徽章 (這裡會拿到你在 DB 看的 19 筆或 7 筆)
        List<GymBadge> badges = gymBadgeRepository.findByJourneyId(journeyId);

        // 2. 撈出該使用者 "SUCCESS" 的道館 ID 列表
        List<Long> passedGymIds = submissionRepository.findByMemberId(userId).stream()
                .filter(s -> s.getStatus() == GymSubmission.SubmissionStatus.SUCCESS)
                .map(s -> s.getGym().getId())
                .toList();

        // 3. 組裝 DTO 並設定 unlocked 狀態
        return badges.stream()
                .map(badge -> GymBadgeDTO.builder()
                        .id(badge.getId())
                        .name(badge.getName())
                        .imageUrl(badge.getImageUrl())
                        .gymId(badge.getGym().getId())
                        .journeyId(badge.getJourneyId())
                        .chapterId(badge.getChapterId())
                        // ★ 核心邏輯：如果 user 通過的道館 ID 包含這個徽章的 gymId，就是 true
                        .unlocked(passedGymIds.contains(badge.getGym().getId()))
                        .build())
                .collect(Collectors.toList());
    }

    // --- Helper: Entity 轉 DTO ---
    private ChallengeRecordDTO toChallengeRecordDTO(GymSubmission entity) {
        return ChallengeRecordDTO.builder()
                .id(entity.getId())
                .userId(entity.getMember().getId())
                .gymId(entity.getGym().getId())
                .gymChallengeId(entity.getChallengeId())
                .status(entity.getStatus().name()) // Enum 轉 String (SUCCESS, FAILED...)
                .feedback(entity.getFeedback())
                .ratings(entity.getRatings()) // Map<String, String> 直接傳遞
                .submission(entity.getSubmissionFiles()) // Map<String, String> 直接傳遞

                // 時間轉換 LocalDateTime -> Long (毫秒)
                .createdAt(toEpochMilli(entity.getSubmittedAt()))
                .bookingCompletedAt(toEpochMilli(entity.getBookingCompletedAt()))
                .completedAt(toEpochMilli(entity.getSubmittedAt()))
                .reviewedAt(toEpochMilli(entity.getReviewedAt()))
                .build();
    }

    private Long toEpochMilli(LocalDateTime ldt) {
        return ldt == null ? null : ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}