package com.waterballsa.tutorial_platform.service;

import com.waterballsa.tutorial_platform.converter.GymChallengeRecordMapper;
import com.waterballsa.tutorial_platform.dto.GymChallengeRecordDTO;
import com.waterballsa.tutorial_platform.entity.Challenge;
import com.waterballsa.tutorial_platform.entity.Gym;
import com.waterballsa.tutorial_platform.entity.GymChallengeRecord;
import com.waterballsa.tutorial_platform.entity.GymSubmission;
import com.waterballsa.tutorial_platform.entity.Member;
import com.waterballsa.tutorial_platform.enums.ChallengeType;
import com.waterballsa.tutorial_platform.event.GymPassedEvent;
import com.waterballsa.tutorial_platform.exception.BusinessException;
import com.waterballsa.tutorial_platform.repository.ChallengeRepository;
import com.waterballsa.tutorial_platform.repository.GymChallengeRecordRepository;
import com.waterballsa.tutorial_platform.repository.GymRepository;
import com.waterballsa.tutorial_platform.repository.GymSubmissionRepository;
import com.waterballsa.tutorial_platform.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GymChallengeRecordService {

    private final GymChallengeRecordRepository repository;
    private final MemberRepository memberRepository;
    private final GymRepository gymRepository;
    private final GymSubmissionRepository gymSubmissionRepository;
    private final ChallengeRepository challengeRepository;
    private final SkillRatingService skillRatingService;
    private final GymChallengeRecordMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * [ARCH-FIX-01] 將 DTO 轉換邏輯移入 Service。
     */
    public List<GymChallengeRecordDTO> getLatestRecordsAsDto(Long userId) {
        List<GymChallengeRecord> records = getLatestRecordsByUserId(userId);
        return records.stream()
                .map(r -> {
                    Challenge challenge = challengeRepository.findById(r.getGymChallengeId()).orElse(null);
                    return mapper.toDto(r, challenge);
                })
                .collect(Collectors.toList());
    }

    public GymChallengeRecordDTO mapToDto(GymChallengeRecord record) {
        if (record == null) return null;
        Challenge challenge = challengeRepository.findById(record.getGymChallengeId()).orElse(null);
        return mapper.toDto(record, challenge);
    }

    public List<GymChallengeRecord> getLatestRecordsByUserId(Long userId) {
        if (userId == null) return Collections.emptyList();
        
        List<GymChallengeRecord> allRecords = repository.findByUserIdOrderByCreatedAtDesc(userId);
        Map<String, GymChallengeRecord> latestRecords = new LinkedHashMap<>();
        for (GymChallengeRecord r : allRecords) {
            String key = r.getGymId() + "_" + r.getGymChallengeId();
            latestRecords.putIfAbsent(key, r);
        }
        return new ArrayList<>(latestRecords.values());
    }

    public List<GymChallengeRecord> getRecordsByGymAndUser(Long gymId, Long userId) {
        return repository.findByGymIdAndUserIdOrderByCreatedAtDesc(gymId, userId);
    }

    public List<GymChallengeRecord> getAllDiagnostics() {
        return repository.findAll();
    }

    @Transactional
    public GymChallengeRecord submitChallengeRecord(Long userId, Long gymId, Long gymChallengeId, Map<String, String> submissionData) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new BusinessException("Gym not found", HttpStatus.NOT_FOUND));

        Long actualChallengeId = gymChallengeId != null ? gymChallengeId : 1L;

        GymChallengeRecord record = repository.findByGymIdAndGymChallengeIdAndUserIdOrderByCreatedAtDesc(
                        gymId, actualChallengeId, userId)
                .stream().findFirst().orElse(new GymChallengeRecord());

        record.setUserId(userId);
        record.setGymId(gymId);
        record.setJourneyId(gym.getJourney() != null ? gym.getJourney().getId() : null);
        record.setChapterId(gym.getChapter() != null ? gym.getChapter().getId() : null);
        record.setGymChallengeId(actualChallengeId);
        record.setSubmission(submissionData != null ? new HashMap<>(submissionData) : new HashMap<>());

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
        syncGymSubmission(member, gym, actualChallengeId, record);

        return record;
    }

    private void syncGymSubmission(Member member, Gym gym, Long challengeId, GymChallengeRecord record) {
        try {
            Challenge challenge = challengeRepository.findById(challengeId).orElse(null);
            if (challenge != null && challenge.getType() == ChallengeType.PRACTICAL_CHALLENGE) {
                List<GymSubmission> existing = gymSubmissionRepository.findByMemberIdAndGymId(member.getId(), gym.getId());
                GymSubmission sub;
                if (existing.isEmpty()) {
                    sub = GymSubmission.builder()
                            .member(member)
                            .gym(gym)
                            .challengeId(record.getGymChallengeId())
                            .status(GymSubmission.SubmissionStatus.SUBMITTED)
                            .grade(0)
                            .submittedAt(LocalDateTime.now())
                            .submissionFiles(record.getSubmission())
                            .build();
                } else {
                    sub = existing.get(0);
                    sub.setChallengeId(record.getGymChallengeId());
                    sub.setStatus(GymSubmission.SubmissionStatus.SUBMITTED);
                    sub.setGrade(0);
                    sub.setFeedback(null);
                    sub.setRatings(new HashMap<>());
                    sub.setSubmittedAt(LocalDateTime.now());
                }
                gymSubmissionRepository.save(sub);
            }
        } catch (Exception e) {
            log.warn("[GymChallengeRecordService] Warn: Failed to sync GymSubmission", e);
        }
    }

    @Transactional
    public GymChallengeRecord bookChallenge(Long userId, Long gymId, Long gymChallengeId) {
        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new BusinessException("Gym not found", HttpStatus.NOT_FOUND));

        Challenge challenge = challengeRepository.findById(gymChallengeId)
                .orElseThrow(() -> new BusinessException("Challenge not found", HttpStatus.NOT_FOUND));

        GymChallengeRecord record = repository.findByGymIdAndGymChallengeIdAndUserIdOrderByCreatedAtDesc(
                        gymId, gymChallengeId, userId)
                .stream().findFirst().orElse(new GymChallengeRecord());

        record.setUserId(userId);
        record.setGymId(gymId);
        record.setJourneyId(gym.getJourney() != null ? gym.getJourney().getId() : null);
        record.setChapterId(gym.getChapter() != null ? gym.getChapter().getId() : null);
        record.setGymChallengeId(gymChallengeId);

        // 設定預約截止時間 (當前時間 + maxDuration)
        int days = (challenge.getMaxDurationInDays() != null) ? challenge.getMaxDurationInDays() : 7;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, days);
        record.setBookingCompletedAt(cal.getTime());

        record.setStatus(GymChallengeRecord.ChallengeStatus.STARTED);
        if (record.getId() == null) {
            record.setCreatedAt(new Date());
        }

        return repository.save(record);
    }

    @Transactional
    public GymChallengeRecord bulkSimulateCorrection(Long userId, Long gymId) {
        List<GymChallengeRecord> records = repository.findByGymIdAndUserIdOrderByCreatedAtDesc(gymId, userId);
        if (records.isEmpty()) {
            throw new BusinessException("找不到該道館的挑戰紀錄", HttpStatus.NOT_FOUND);
        }

        String feedback = "導師批改：實作優秀！優點：結構清晰。";
        Map<String, String> ratings = new HashMap<>();
        // 直接使用通用維度，模擬更真實的批改數據
        ratings.put("Logic", "S");
        ratings.put("Arch", "A");
        ratings.put("Design", "B");

        for (GymChallengeRecord target : records) {
            target.setStatus(GymChallengeRecord.ChallengeStatus.SUCCESS);
            target.setFeedback(feedback);
            target.setRatings(ratings);
            target.setReviewedAt(new Date());
            target.setCompletedAt(new Date());
            repository.save(target);
        }

        // [ISSUE-28-05-06-01] 手動更新技能評級 (僅限實作挑戰)
        Member member = memberRepository.findById(userId).orElse(null);
        if (member != null) {
            Challenge challenge = challengeRepository.findById(records.get(0).getGymChallengeId()).orElse(null);
            if (challenge != null && challenge.getType() == ChallengeType.PRACTICAL_CHALLENGE) {
                skillRatingService.updateSkillRating(member, ratings, records.get(0).getJourneyId());
            }
        }

        // 重要：發佈過關事件，並標註 skipSkillUpdate=true，因為我們剛才已經手動更新過了
        eventPublisher.publishEvent(new GymPassedEvent(userId, gymId, true));

        repository.flush();
        return records.get(0);
    }

    @Transactional
    public GymChallengeRecord forceGradeRecord(Long recordId) {
        GymChallengeRecord target = repository.findById(recordId)
                .orElseThrow(() -> new BusinessException("Record not found", HttpStatus.NOT_FOUND));

        target.setStatus(GymChallengeRecord.ChallengeStatus.SUCCESS);
        target.setFeedback("手動強行批改成功");
        target.setReviewedAt(new Date());
        target.setCompletedAt(new Date());
        repository.saveAndFlush(target);

        // 重要：發佈過關事件 (ISSUE-BADGE-02-01)
        eventPublisher.publishEvent(new GymPassedEvent(target.getUserId(), target.getGymId()));

        return target;
    }
}
