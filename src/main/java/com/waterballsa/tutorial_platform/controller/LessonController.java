package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.entity.Lesson;
import com.waterballsa.tutorial_platform.entity.LessonContent;
import com.waterballsa.tutorial_platform.entity.Reward;
import com.waterballsa.tutorial_platform.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class LessonController {

    private final LessonRepository lessonRepository;

    @GetMapping("/{id}")
    public ResponseEntity<?> getLesson(@PathVariable Long id) {
        // 這裡會先去 DB 找 Lesson，如果找到，再轉成 DTO
        return lessonRepository.findById(id)
                .map(this::toFrontendDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 將 Lesson 實體轉換為前端需要的 JSON 格式
    private Map<String, Object> toFrontendDto(Lesson lesson) {
        return Map.of(
                "id", lesson.getId(),
                "name", lesson.getName(),
                "description", lesson.getDescription() != null ? lesson.getDescription() : "",
                // 如果 Lesson 本身有 type 就用，沒有就預設 video
                "type", lesson.getType() != null ? lesson.getType().toLowerCase() : "video",
                "createdAt", System.currentTimeMillis(), // 前端介面有 createdAt，補上一個時間戳
                // ★★★ 關鍵：這裡呼叫轉換內容的方法，把 Content 串進來 ★★★
                "content", toContentDtoList(lesson.getContents()),
                "reward", toRewardDto(lesson)
        );
    }

    // ★★★ 這裡就是「Lesson 串 LessonContent」取 URL 的核心邏輯 ★★★
    private List<Map<String, Object>> toContentDtoList(List<LessonContent> contents) {
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