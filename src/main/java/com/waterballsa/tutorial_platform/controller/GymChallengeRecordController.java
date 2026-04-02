package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.converter.GymChallengeRecordMapper;
import com.waterballsa.tutorial_platform.dto.GymChallengeRecordDTO;
import com.waterballsa.tutorial_platform.entity.GymChallengeRecord;
import com.waterballsa.tutorial_platform.exception.BusinessException;
import com.waterballsa.tutorial_platform.service.GymChallengeRecordService;
import com.waterballsa.tutorial_platform.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ray (Antigravity AI)
 * 重構版本 (Phase 6): 將業務邏輯下放至 Service，統一例外處理，移除 Try-Catch。
 */
@Slf4j
@RestController
@RequestMapping("/api/gym-challenge-records")
@RequiredArgsConstructor
public class GymChallengeRecordController {

    private final GymChallengeRecordService recordService;
    private final MemberService memberService;
    private final GymChallengeRecordMapper mapper;

    @GetMapping("/user/{userId}")
    public List<GymChallengeRecordDTO> getRecords(@PathVariable Long userId) {
        log.info("[RecordController] Fetching records for user {}", userId);
        return recordService.getLatestRecordsByUserId(userId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/me")
    public List<GymChallengeRecordDTO> getMyRecords(Authentication auth) {
        Long userId = memberService.getCurrentMemberId(auth);
        if (userId == null) {
            return Collections.emptyList();
        }
        return getRecords(userId);
    }

    @PostMapping("")
    public GymChallengeRecordDTO submitRecord(Authentication auth, @RequestBody GymChallengeRecordDTO dto) {
        Long userId = memberService.getCurrentMemberId(auth);
        if (userId == null) {
            throw new BusinessException("Unauthorized", HttpStatus.UNAUTHORIZED);
        }
        log.info("[RecordController] Submitting record for user {} gym {}", userId, dto.getGymId());
        
        GymChallengeRecord record = recordService.submitChallengeRecord(userId, dto.getGymId(), dto.getGymChallengeId(), dto.getSubmission());
        return mapper.toDto(record);
    }

    @PostMapping("/book")
    public GymChallengeRecordDTO bookChallenge(Authentication auth, @RequestBody GymChallengeRecordDTO dto) {
        Long userId = memberService.getCurrentMemberId(auth);
        if (userId == null) {
            throw new BusinessException("Unauthorized", HttpStatus.UNAUTHORIZED);
        }
        log.info("[RecordController] Booking challenge for user {} gym {}", userId, dto.getGymId());
        
        GymChallengeRecord record = recordService.bookChallenge(userId, dto.getGymId(), dto.getGymChallengeId());
        return mapper.toDto(record);
    }

    @PostMapping("/demo/simulate-correction/{gymId}")
    public ResponseEntity<?> simulateCorrection(Authentication auth, @PathVariable Long gymId) {
        Long userId = memberService.getCurrentMemberId(auth);
        if (userId == null) {
            throw new BusinessException("Unauthorized", HttpStatus.UNAUTHORIZED);
        }
        log.info("[RecordController] Bulk simulateCorrection for gymId {}, userId {}", gymId, userId);
        
        GymChallengeRecord sampleRecord = recordService.bulkSimulateCorrection(userId, gymId);
        return ResponseEntity.ok(mapper.toDto(sampleRecord));
    }

    @PostMapping("/demo/force-grade-record-by-id/{id}")
    public ResponseEntity<?> forceGradeRecord(@PathVariable Long id) {
        log.info("[RecordController] Force grading record ID: {}", id);
        GymChallengeRecord record = recordService.forceGradeRecord(id);
        return ResponseEntity.ok(mapper.toDto(record));
    }

    @GetMapping("/diagnostic/all")
    public List<GymChallengeRecord> getAllRecords() {
        return recordService.getAllDiagnostics();
    }
}