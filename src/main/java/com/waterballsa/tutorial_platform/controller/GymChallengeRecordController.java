package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.dto.GymChallengeRecordDTO;
import com.waterballsa.tutorial_platform.entity.GymChallengeRecord;
import com.waterballsa.tutorial_platform.repository.GymChallengeRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users/{userId}/journeys/gyms/challenges/records")
@RequiredArgsConstructor
public class GymChallengeRecordController {

    private final GymChallengeRecordRepository repository;

    @GetMapping
    public List<GymChallengeRecordDTO> getRecords(@PathVariable Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private GymChallengeRecordDTO toDto(GymChallengeRecord entity) {
        GymChallengeRecordDTO dto = new GymChallengeRecordDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setJourneyId(entity.getJourneyId());
        dto.setChapterId(entity.getChapterId());
        dto.setGymId(entity.getGymId());
        dto.setGymChallengeId(entity.getGymChallengeId());
        dto.setStatus(entity.getStatus().name());
        dto.setFeedback(entity.getFeedback());
        dto.setRatings(entity.getRatings());
        dto.setSubmission(entity.getSubmission());

        // 處理日期轉 Timestamp
        if (entity.getCreatedAt() != null) dto.setCreatedAt(entity.getCreatedAt().getTime());
        if (entity.getCompletedAt() != null) dto.setCompletedAt(entity.getCompletedAt().getTime());
        if (entity.getReviewedAt() != null) dto.setReviewedAt(entity.getReviewedAt().getTime());
        if (entity.getBookingCompletedAt() != null) dto.setBookingCompletedAt(entity.getBookingCompletedAt().getTime());
        return dto;
    }
}