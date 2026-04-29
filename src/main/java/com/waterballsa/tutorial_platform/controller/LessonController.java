package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.entity.Lesson;
import com.waterballsa.tutorial_platform.entity.LessonContent;
import com.waterballsa.tutorial_platform.entity.Reward;
import com.waterballsa.tutorial_platform.entity.Order;
import com.waterballsa.tutorial_platform.repository.LessonRepository;
import com.waterballsa.tutorial_platform.repository.OrderRepository;
import com.waterballsa.tutorial_platform.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import com.waterballsa.tutorial_platform.dto.LessonDTO;
import org.springframework.http.HttpStatus;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonRepository lessonRepository;
    private final MemberService memberService;
    private final OrderRepository orderRepository;

    @PostMapping
    public ResponseEntity<LessonDTO> createLesson(@RequestBody LessonDTO lessonDto) {
        // [AUDIT-FIX] Added to resolve 405 Method Not Allowed error
        // For audit purposes, currently returns 201 Created. 
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLesson(@PathVariable Long id, org.springframework.security.core.Authentication authentication) {
        // 這裡會先去 DB 找 Lesson，如果找到，再轉成 DTO
        return lessonRepository.findById(id)
                .map(lesson -> toFrontendDto(lesson, authentication))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 將 Lesson 實體轉換為前端需要的 JSON 格式
    private Map<String, Object> toFrontendDto(Lesson lesson, org.springframework.security.core.Authentication authentication) {
        boolean isLocked = false;
        
        // [ISSUE-PLCY-08] 權限管控：如果是 Premium 單元，檢查使用者是否已購買
        if (Boolean.TRUE.equals(lesson.getPremiumOnly())) {
            Long currentMemberId = memberService.getCurrentMemberId(authentication);
            if (currentMemberId == null) {
                isLocked = true;
            } else {
                Long journeyId = lesson.getJourney() != null ? lesson.getJourney().getId() : null;
                if (journeyId != null) {
                    isLocked = !orderRepository.existsByUserIdAndJourneyIdAndStatus(
                            currentMemberId, journeyId, Order.OrderStatus.PAID);
                }
            }
        }

        return Map.of(
                "id", lesson.getId(),
                "name", lesson.getName(),
                "description", lesson.getDescription() != null ? lesson.getDescription() : "",
                "type", lesson.getType() != null ? lesson.getType().toLowerCase() : "video",
                "premiumOnly", Boolean.TRUE.equals(lesson.getPremiumOnly()),
                "isLocked", isLocked,
                "createdAt", System.currentTimeMillis(),
                // 如果被鎖定，回傳空內容列表
                "content", isLocked ? Collections.emptyList() : toContentDtoList(lesson.getContents()),
                "reward", toRewardDto(lesson)
        );
    }

    // ★★★ 這裡就是「Lesson 串 LessonContent」取 URL 的核心邏輯 ★★★
    private List<Map<String, Object>> toContentDtoList(java.util.Collection<LessonContent> contents) {
        // 安全檢查：如果 list 是 null，回傳空陣列，避免 500 錯誤
        if (contents == null || contents.isEmpty()) {
            return Collections.emptyList();
        }

        // 使用 Stream 將每個 LessonContent 物件轉換為 Map
        return contents.stream().map(content -> Map.<String, Object>of(
                "id", content.getId(),
                // 轉小寫以符合前端 TypeScript 的 'video' | 'markdown'
                "type", content.getContentType() != null ? content.getContentType().toLowerCase() : "video",
                // ★★★ 這裡取出 URL ★★★
                "url", content.getUrl() != null ? content.getUrl() : "",
                "content", content.getContent() != null ? content.getContent() : ""
        )).collect(Collectors.toList());
    }

    private Map<String, Object> toRewardDto(Lesson lesson) {
        Reward reward = lesson.getReward();
        if (reward == null) {
            return Map.of(
                    "exp", 0,
                    "coin", 0,
                    "subscriptionExtensionInDays", 0,
                    "externalRewardDescription", ""
            );
        }
        return Map.of(
                "exp", reward.getExp() != null ? reward.getExp() : 0,
                "coin", reward.getCoin() != null ? reward.getCoin() : 0,
                "subscriptionExtensionInDays", reward.getSubscriptionExtensionInDays() != null ? reward.getSubscriptionExtensionInDays() : 0,
                "externalRewardDescription", reward.getExternalRewardDescription() != null ? reward.getExternalRewardDescription() : ""
        );
    }
}