package com.waterballsa.tutorial_platform.converter;

import com.waterballsa.tutorial_platform.dto.GymChallengeRecordDTO;
import com.waterballsa.tutorial_platform.entity.Challenge;
import com.waterballsa.tutorial_platform.entity.GymChallengeRecord;
import com.waterballsa.tutorial_platform.repository.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class GymChallengeRecordMapper {

    private final ChallengeRepository challengeRepository;

    public GymChallengeRecordDTO toDto(GymChallengeRecord entity) {
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

            // 查找挑戰類型 (區分實戰 vs 速解)
            try {
                challengeRepository.findById(entity.getGymChallengeId())
                        .ifPresent(c -> dto.setChallengeType(c.getType()));
            } catch (Exception ignore) {
            }

            return dto;
        } catch (Exception e) {
            log.error("[GymChallengeRecordMapper] DTO mapping fatal error", e);
            GymChallengeRecordDTO err = new GymChallengeRecordDTO();
            err.setId(entity.getId());
            err.setStatus("ERROR");
            return err;
        }
    }
}
