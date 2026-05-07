package com.waterballsa.tutorial_platform.converter;

import com.waterballsa.tutorial_platform.dto.GymChallengeRecordDTO;
import com.waterballsa.tutorial_platform.entity.Challenge;
import com.waterballsa.tutorial_platform.entity.GymChallengeRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Slf4j
@Component
public class GymChallengeRecordMapper {

    /**
     * [ARCH-FIX-01] 移除 Repository 依賴，回歸純粹轉換職責。
     * 呼叫端 (Service) 負責準備好 Challenge 物件。
     */
    public GymChallengeRecordDTO toDto(GymChallengeRecord entity, Challenge challenge) {
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

            // 標題顯示處理
            dto.setGymName("道館挑戰 #" + entity.getGymId()); // Default Fallback

            if (challenge != null) {
                dto.setChallengeType(challenge.getType());
                if (challenge.getName() != null && !challenge.getName().isEmpty()) {
                    dto.setGymName(challenge.getName());
                }
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

    /**
     * 簡易版轉換 (不含挑戰詳細資訊)
     */
    public GymChallengeRecordDTO toDto(GymChallengeRecord entity) {
        return toDto(entity, null);
    }
}
