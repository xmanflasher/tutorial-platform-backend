package com.waterballsa.tutorial_platform.service;

import com.waterballsa.tutorial_platform.dto.ChallengeRecordDTO;
import com.waterballsa.tutorial_platform.dto.GymBadgeDTO;
import com.waterballsa.tutorial_platform.dto.GymStatusDTO;
import com.waterballsa.tutorial_platform.dto.GymDetailDTO;
import com.waterballsa.tutorial_platform.entity.Gym;
import com.waterballsa.tutorial_platform.entity.GymBadge;
import com.waterballsa.tutorial_platform.entity.GymSubmission;
import com.waterballsa.tutorial_platform.repository.GymBadgeRepository;
import com.waterballsa.tutorial_platform.repository.GymRepository;
import com.waterballsa.tutorial_platform.repository.GymSubmissionRepository;
import com.waterballsa.tutorial_platform.entity.MemberBadge;
import com.waterballsa.tutorial_platform.repository.MemberBadgeRepository;
import com.waterballsa.tutorial_platform.converter.GymMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GymService {

    private final GymRepository gymRepository;
    private final GymSubmissionRepository submissionRepository;
    private final GymBadgeRepository gymBadgeRepository;
    private final MemberBadgeRepository memberBadgeRepository;
    private final GymMapper gymMapper;

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
            boolean hasPassed = submissions.stream()
                    .anyMatch(s -> s.getGym() != null && s.getGym().getId().equals(gym.getId())
                            && s.getStatus() == GymSubmission.SubmissionStatus.SUCCESS);

            // 取得該 Gym 最高分的紀錄 (用來顯示星數)
            Integer maxGrade = submissions.stream()
                    .filter(s -> s.getGym() != null && s.getGym().getId().equals(gym.getId()))
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

        // 2. 撈出該使用者已獲得的徽章 ID 列表
        List<Long> unlockedBadgeIds = memberBadgeRepository.findByMemberId(userId).stream()
                .map(MemberBadge::getBadgeId)
                .toList();

        log.info("🔍 [Debug Badge] UserID: {}, JourneyID: {}", userId, journeyId);
        log.info("🔍 [Debug Badge] Unlocked IDs found in DB: {}", unlockedBadgeIds);

        // 3. 組裝 DTO 並設定 unlocked 狀態
        return badges.stream()
                .map(badge -> {
                    if (badge.getId() == null) {
                        log.warn("⚠️ [Debug Badge] Badge has NULL ID! Name: {}", badge.getName());
                        return GymBadgeDTO.builder().name(badge.getName()).unlocked(false).build();
                    }

                    // 強制轉型比對 (避免 Long vs Integer 陷阱)
                    boolean isUnlocked = unlockedBadgeIds.stream().anyMatch(unlockedId -> unlockedId.equals(badge.getId()));
                    
                    log.info("🔍 [Debug Badge] Checking Badge: '{}' (ID: {}). Match found: {}", 
                        badge.getName(), badge.getId(), isUnlocked);
                    
                    return GymBadgeDTO.builder()
                        .id(badge.getId())
                        .name(badge.getName())
                        .imageUrl(badge.getImageUrl())
                        .gymId(badge.getGym() != null ? badge.getGym().getId() : null)
                        .journeyId(badge.getJourneyId())
                        .chapterId(badge.getChapterId())
                        .unlocked(isUnlocked)
                        .build();
                })
                .collect(Collectors.toList());
    }

    // --- 功能 3.5: 取得未播放動畫的徽章 (Data-Driven Celebration) ---
    public List<GymBadgeDTO> getUnshownBadges(Long userId) {
        // 1. 撈出該死者所有 `is_shown = false` 的 member_badges
        List<MemberBadge> unshownMemberBadges = memberBadgeRepository.findByMemberIdAndIsShownFalse(userId);
        
        List<GymBadgeDTO> unshownDtos = new ArrayList<>();
        for (MemberBadge mb : unshownMemberBadges) {
            gymBadgeRepository.findById(mb.getBadgeId()).ifPresent(badge -> {
                unshownDtos.add(GymBadgeDTO.builder()
                        .id(badge.getId())
                        .name(badge.getName())
                        .imageUrl(badge.getImageUrl())
                        .gymId(badge.getGym() != null ? badge.getGym().getId() : null)
                        .journeyId(badge.getJourneyId())
                        .chapterId(badge.getChapterId())
                        .unlocked(true)
                        .build());
            });
        }
        return unshownDtos;
    }

    // --- 功能 3.6: 標記徽章動畫為已播放 ---
    @Transactional
    public void markBadgeAsShown(Long userId, Long badgeId) {
        memberBadgeRepository.findByMemberIdAndBadgeId(userId, badgeId).ifPresent(mb -> {
            mb.setIsShown(true);
            memberBadgeRepository.save(mb);
            log.info("✅ 徽章標記為已播放: User={}, Badge={}", userId, badgeId);
        });
    }

    // --- 功能 4: 取得特定道館的詳細資料 (包含 Mapper 轉換) ---
    public GymDetailDTO getGymDetail(Long id) {
        return gymRepository.findById(id)
                .map(gymMapper::toDetailDTO)
                .orElse(null);
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