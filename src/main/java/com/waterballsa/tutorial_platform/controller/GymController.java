package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.dto.ChallengeDTO;
import com.waterballsa.tutorial_platform.dto.GymBadgeDTO;
import com.waterballsa.tutorial_platform.dto.GymDetailDTO;
import com.waterballsa.tutorial_platform.dto.LessonDTO;
import com.waterballsa.tutorial_platform.entity.Gym;
import com.waterballsa.tutorial_platform.repository.GymRepository;
import com.waterballsa.tutorial_platform.service.GymService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class GymController {

    private final GymService gymService;
    private final GymRepository gymRepository;

    @GetMapping("/journeys/{journeyId}/gym-badges")
    public ResponseEntity<List<GymBadgeDTO>> getGymBadges(
            @PathVariable Long journeyId,
            @RequestParam(required = false, defaultValue = "0") Long userId
    ) {
        return ResponseEntity.ok(gymService.getBadgesByJourney(userId, journeyId));
    }

    // 2. 取得特定道館的詳細資料 (給 GymDetailView 用)
    @GetMapping("/gyms/{id}")
    public ResponseEntity<GymDetailDTO> getGymDetail(@PathVariable Long id) {
        // ❌ 錯誤寫法 (原本的)：這會去查 DB 主鍵，導致 404
        // return gymRepository.findById(id) ...

        // ✅ 正確寫法：改用 original_id 查詢
        // 這樣前端傳 8，我們就去資料庫找 findById = 8 的那筆資料
        return gymRepository.findById(id)
                .map(this::toDetailDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- Helper Method: Entity 轉 DTO ---
// 在 GymController 或 GymService 中轉換 DTO 時
    private GymDetailDTO toDetailDTO(Gym gym) {
        return GymDetailDTO.builder()
                .id(gym.getId())
                .name(gym.getName())
                .description(gym.getDescription())
                // ★★★ 1. 修正 challenges: null 的問題 ★★★
                // 必須手動把 Entity 轉成 DTO List，原本漏寫了這段
                .challenges(gym.getChallenges() == null ? Collections.emptyList() : gym.getChallenges().stream()
                        .map(c -> ChallengeDTO.builder()
                                .id(c.getId())
                                .name(c.getName())
                                .type(c.getType()) // Enum 會自動轉字串
                                .recommendDurationInDays(c.getRecommendDurationInDays())
                                .maxDurationInDays(c.getMaxDurationInDays())
                                .build())
                        .collect(Collectors.toList()))
                // ★★★ 2. 修正 lessons: [] 的問題 ★★★
                // 確保讀取 relatedLessons 並轉成 DTO
                // (如果這裡還是空，請檢查資料庫 lessons 表的 gym_id 是否有值)
                .lessons(gym.getRelatedLessons() == null ? Collections.emptyList() : gym.getRelatedLessons().stream()
                        .map(l -> LessonDTO.builder()
                                .id(String.valueOf(l.getId()))
                                .name(l.getName())
                                .type(l.getType())
                                .build())
                        .collect(Collectors.toList()))
                .rewardExp(gym.getRewardExp())
                .build();
    }
}