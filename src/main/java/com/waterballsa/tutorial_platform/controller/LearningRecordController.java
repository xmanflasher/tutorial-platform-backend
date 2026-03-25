package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.entity.LearningRecord;
import com.waterballsa.tutorial_platform.repository.LearningRecordRepository;
import com.waterballsa.tutorial_platform.service.LearningService;
import com.waterballsa.tutorial_platform.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/learning-records")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class LearningRecordController {

    private final LearningService learningService;
    private final LearningRecordRepository recordRepository;
    private final MemberService memberService;

    @GetMapping("/me/finished-lessons")
    public ResponseEntity<List<Long>> getMyFinishedLessons(Authentication auth) {
        Long userId = memberService.getCurrentMemberId(auth);
        if (userId == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<LearningRecord> records = recordRepository.findByMemberId(userId);
        
        // Filter those marked as finished and extract lesson IDs
        List<Long> finishedLessonIds = records.stream()
                .filter(LearningRecord::isFinished)
                .map(record -> record.getLesson().getId())
                .collect(Collectors.toList());

        return ResponseEntity.ok(finishedLessonIds);
    }

    @PostMapping("/lessons/{lessonId}/complete")
    public ResponseEntity<Void> completeLesson(@PathVariable Long lessonId, Authentication auth) {
        Long userId = memberService.getCurrentMemberId(auth);
        if (userId == null) {
            // Unauthorized check - handled by Spring Security technically, but safe guard here
            return ResponseEntity.status(401).build();
        }

        learningService.markLessonAsComplete(userId, lessonId);
        return ResponseEntity.ok().build();
    }
}
