package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.dto.GymChallengeRecordDTO;
import com.waterballsa.tutorial_platform.entity.GymChallengeRecord;
import com.waterballsa.tutorial_platform.entity.GymSubmission;
import com.waterballsa.tutorial_platform.repository.GymRepository;
import com.waterballsa.tutorial_platform.repository.GymChallengeRecordRepository;
import com.waterballsa.tutorial_platform.repository.GymSubmissionRepository;
import com.waterballsa.tutorial_platform.repository.MemberRepository;
import com.waterballsa.tutorial_platform.entity.Challenge;
import com.waterballsa.tutorial_platform.repository.ChallengeRepository;
import com.waterballsa.tutorial_platform.enums.ChallengeType;
import com.waterballsa.tutorial_platform.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Ray (Antigravity AI)
 * 極簡化版本，確保絕對不會發生 500 錯誤 (或至少能捕捉到它)
 */
@Slf4j
@RestController
@RequestMapping("/api/gym-challenge-records")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GymChallengeRecordController {

    private final GymChallengeRecordRepository repository;
    private final MemberRepository memberRepository;
    private final GymRepository gymRepository;
    private final GymSubmissionRepository gymSubmissionRepository;
    private final ChallengeRepository challengeRepository;
    private final MemberService memberService;

    @GetMapping("/user/{userId}")
    public List<GymChallengeRecordDTO> getRecords(@PathVariable Long userId) {
        log.info("[RecordController] Fetching records for user {}", userId);
        if (userId == null) return Collections.emptyList();
        
        try {
            List<GymChallengeRecord> allRecords = repository.findByUserIdOrderByCreatedAtDesc(userId);
            
            // Key 為 gymId + "_" + gymChallengeId，確保同一個道館的不同挑戰分開記錄
            Map<String, GymChallengeRecord> latestRecords = new LinkedHashMap<>();
            for (GymChallengeRecord r : allRecords) {
                String key = r.getGymId() + "_" + r.getGymChallengeId();
                if (!latestRecords.containsKey(key)) {
                    latestRecords.put(key, r);
                }
            }

            return latestRecords.values().stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("[RecordController] getRecords error", e);
            return Collections.emptyList();
        }
    }

    @GetMapping("/me")
    public List<GymChallengeRecordDTO> getMyRecords(org.springframework.security.core.Authentication auth) {
        try {
            Long userId = memberService.getCurrentMemberId(auth);
            return getRecords(userId);
        } catch (Exception e) {
            log.error("[RecordController] getMyRecords auth error", e);
            return Collections.emptyList();
        }
    }

    @PostMapping("")
    @Transactional
    public GymChallengeRecordDTO submitRecord(
            org.springframework.security.core.Authentication auth,
            @RequestBody GymChallengeRecordDTO dto) {
        
        Long userId = memberService.getCurrentMemberId(auth);
        if (userId == null) throw new RuntimeException("Unauthorized");

        log.info("[RecordController] Submitting record for user {} gym {}", userId, dto.getGymId());

        com.waterballsa.tutorial_platform.entity.Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        com.waterballsa.tutorial_platform.entity.Gym gym = gymRepository.findById(dto.getGymId())
                .orElseThrow(() -> new RuntimeException("Gym not found"));

        // ★ 核心修復：使用特定 (gymId, gymChallengeId, userId) 查找，防止蓋過同道館的其他挑戰
        GymChallengeRecord record = repository.findByGymIdAndGymChallengeIdAndUserIdOrderByCreatedAtDesc(
                        dto.getGymId(), dto.getGymChallengeId(), userId)
                .stream().findFirst().orElse(new GymChallengeRecord());
        
        record.setUserId(userId);
        record.setGymId(dto.getGymId());
        record.setJourneyId(gym.getJourney() != null ? gym.getJourney().getId() : null);
        record.setChapterId(gym.getChapter() != null ? gym.getChapter().getId() : null);
        record.setGymChallengeId(dto.getGymChallengeId() != null ? dto.getGymChallengeId() : 1L);
        record.setSubmission(dto.getSubmission() != null ? new HashMap<>(dto.getSubmission()) : new HashMap<>());
        
        // 重置批改資訊
        record.setStatus(GymChallengeRecord.ChallengeStatus.SUBMITTED);
        record.setFeedback(null);
        record.setRatings(new HashMap<>());
        record.setReviewedAt(null);
        record.setCompletedAt(null);
        
        if (record.getId() == null) {
            record.setCreatedAt(new Date());
        }
        repository.save(record);

        // 同步至 GymSubmission (僅限實作挑戰)
        try {
            Challenge challenge = challengeRepository.findById(dto.getGymChallengeId()).orElse(null);
            if (challenge != null && challenge.getType() == ChallengeType.PRACTICAL_CHALLENGE) {
                List<GymSubmission> existing = gymSubmissionRepository.findByMemberIdAndGymId(userId, dto.getGymId());
                if (existing.isEmpty()) {
                    GymSubmission sub = GymSubmission.builder()
                            .member(member).gym(gym).challengeId(record.getGymChallengeId())
                            .status(GymSubmission.SubmissionStatus.SUBMITTED)
                            .grade(0).submittedAt(LocalDateTime.now())
                            .submissionFiles(record.getSubmission()).build();
                    gymSubmissionRepository.save(sub);
                } else {
                    GymSubmission sub = existing.get(0);
                    sub.setChallengeId(record.getGymChallengeId()); // 更新當前實戰挑戰 ID
                    sub.setStatus(GymSubmission.SubmissionStatus.SUBMITTED);
                    sub.setGrade(0); sub.setFeedback(null); sub.setRatings(new HashMap<>());
                    sub.setSubmittedAt(LocalDateTime.now());
                    gymSubmissionRepository.save(sub);
                }
            }
        } catch (Exception e) {
            log.error("[RecordController] Sync error", e);
        }

        return toDto(record);
    }

    @PostMapping("/book")
    @Transactional
    public GymChallengeRecordDTO bookChallenge(
            org.springframework.security.core.Authentication auth,
            @RequestBody GymChallengeRecordDTO dto) {
        
        Long userId = memberService.getCurrentMemberId(auth);
        if (userId == null) throw new RuntimeException("Unauthorized");

        log.info("[RecordController] Booking challenge for user {} gym {}", userId, dto.getGymId());

        com.waterballsa.tutorial_platform.entity.Gym gym = gymRepository.findById(dto.getGymId())
                .orElseThrow(() -> new RuntimeException("Gym not found"));

        Challenge challenge = challengeRepository.findById(dto.getGymChallengeId())
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        // 查找或建立紀錄
        GymChallengeRecord record = repository.findByGymIdAndGymChallengeIdAndUserIdOrderByCreatedAtDesc(
                        dto.getGymId(), dto.getGymChallengeId(), userId)
                .stream().findFirst().orElse(new GymChallengeRecord());
        
        record.setUserId(userId);
        record.setGymId(dto.getGymId());
        record.setJourneyId(gym.getJourney() != null ? gym.getJourney().getId() : null);
        record.setChapterId(gym.getChapter() != null ? gym.getChapter().getId() : null);
        record.setGymChallengeId(dto.getGymChallengeId());
        
        // 設定預約截止時間 (當前時間 + maxDuration)
        int days = (challenge.getMaxDurationInDays() != null) ? challenge.getMaxDurationInDays() : 7;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, days);
        record.setBookingCompletedAt(cal.getTime());
        
        record.setStatus(GymChallengeRecord.ChallengeStatus.STARTED); 
        if (record.getId() == null) {
            record.setCreatedAt(new Date());
        }
        
        repository.save(record);
        return toDto(record);
    }

    @PostMapping("/demo/simulate-correction/{gymId}")
    @Transactional
    public ResponseEntity<?> simulateCorrection(
            org.springframework.security.core.Authentication auth,
            @PathVariable Long gymId) {
        
        log.info("[RecordController] Bulk simulateCorrection START for gymId (used for context) {}", gymId);
        
        try {
            Long userId = memberService.getCurrentMemberId(auth);
            if (userId == null) return ResponseEntity.status(401).body("Unauthorized");

            // 取得該使用者的所有紀錄
            List<GymChallengeRecord> records = repository.findByUserIdOrderByCreatedAtDesc(userId);
            if (records.isEmpty()) {
                return ResponseEntity.status(404).body("找不到任何挑戰紀錄");
            }

            String feedback = "導師批改：實作優秀！優點：結構清晰。";
            Map<String, String> ratings = new HashMap<>();
            ratings.put("1", "S"); ratings.put("2", "A");

            for (GymChallengeRecord target : records) {
                target.setStatus(GymChallengeRecord.ChallengeStatus.SUCCESS);
                target.setFeedback(feedback);
                target.setRatings(ratings);
                target.setReviewedAt(new Date());
                target.setCompletedAt(new Date());
                repository.save(target);
            }
            
            repository.flush();
            log.info("[RecordController] Bulk update finished for userId {}", userId);

            return ResponseEntity.ok(toDto(records.get(0)));

        } catch (Exception e) {
            log.error("[RecordController] Bulk update error", e);
            return ResponseEntity.status(500).body("批改失敗: " + e.getMessage());
        }
    }

    @PostMapping("/demo/force-grade-record-by-id/{id}")
    @Transactional
    public ResponseEntity<?> forceGradeRecord(@PathVariable Long id) {
        log.info("[RecordController] Force grading record ID: {}", id);
        
        try {
            GymChallengeRecord target = repository.findById(id).orElseThrow();

            target.setStatus(GymChallengeRecord.ChallengeStatus.SUCCESS);
            target.setFeedback("手動強行批改成功");
            target.setReviewedAt(new Date());
            target.setCompletedAt(new Date());
            
            repository.saveAndFlush(target);
            log.info("[RecordController] Record {} force-graded successfully", id);

            return ResponseEntity.ok(toDto(target));
        } catch (Exception e) {
            log.error("[RecordController] Force grade error", e);
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/diagnostic/all")
    public List<GymChallengeRecord> getAllRecords() {
        return repository.findAll();
    }

    private GymChallengeRecordDTO toDto(GymChallengeRecord entity) {
        if (entity == null) return null;
        try {
            GymChallengeRecordDTO dto = new GymChallengeRecordDTO();
            dto.setId(entity.getId());
            dto.setUserId(entity.getUserId());
            dto.setJourneyId(entity.getJourneyId());
            dto.setChapterId(entity.getChapterId());
            dto.setGymId(entity.getGymId());
            dto.setGymChallengeId(entity.getGymChallengeId());
            dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : "SUBMITTED");
            dto.setFeedback(entity.getFeedback());
            dto.setRatings(entity.getRatings() != null ? new HashMap<>(entity.getRatings()) : new HashMap<>());
            dto.setSubmission(entity.getSubmission() != null ? new HashMap<>(entity.getSubmission()) : new HashMap<>());

            if (entity.getCreatedAt() != null) dto.setCreatedAt(entity.getCreatedAt().getTime());
            if (entity.getCompletedAt() != null) dto.setCompletedAt(entity.getCompletedAt().getTime());
            if (entity.getReviewedAt() != null) dto.setReviewedAt(entity.getReviewedAt().getTime());
            if (entity.getBookingCompletedAt() != null) dto.setBookingCompletedAt(entity.getBookingCompletedAt().getTime());
            
            dto.setGymName("道館挑戰 #" + entity.getGymId());
            
            // ★ 查找挑戰類型 (區分實戰 vs 速解)
            try {
                challengeRepository.findById(entity.getGymChallengeId())
                    .ifPresent(c -> dto.setChallengeType(c.getType()));
            } catch (Exception ignore) {}
            
            return dto;
        } catch (Exception e) {
            log.error("[RecordController] DTO mapping fatal error", e);
            GymChallengeRecordDTO err = new GymChallengeRecordDTO();
            err.setId(entity.getId());
            err.setStatus("ERROR");
            return err;
        }
    }
}