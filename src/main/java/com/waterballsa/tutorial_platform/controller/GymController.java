package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.dto.ChallengeRecordDTO;
import com.waterballsa.tutorial_platform.dto.GymBadgeDTO;
import com.waterballsa.tutorial_platform.service.GymService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class GymController {

    private final GymService gymService;

    // 1. 取得使用者挑戰歷程
    @GetMapping("/users/{userId}/journeys/gyms/challenges/records")
    public ResponseEntity<List<ChallengeRecordDTO>> getChallengeRecords(@PathVariable Long userId) {
        return ResponseEntity.ok(gymService.getUserChallengeRecords(userId));
    }

    // 2. 取得旅程徽章 (★ 修改重點)
    // 加上 @RequestParam Long userId，讓前端傳入 "是誰要看這些徽章"
    @GetMapping("/journeys/{journeyId}/gym-badges")
    public ResponseEntity<List<GymBadgeDTO>> getGymBadges(
            @PathVariable Long journeyId,
            @RequestParam(required = false, defaultValue = "0") Long userId // 預設 0 代表未登入/無使用者
    ) {
        // ★ 關鍵修正：呼叫 Service 真正去 DB 撈資料
        return ResponseEntity.ok(gymService.getBadgesByJourney(userId, journeyId));
    }
}