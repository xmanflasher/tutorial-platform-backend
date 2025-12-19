package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.entity.Lesson;
import com.waterballsa.tutorial_platform.entity.LessonContent;
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
@CrossOrigin(origins = "http://localhost:3000") // 允許前端存取
public class LessonController {

    private final LessonRepository lessonRepository;

    @GetMapping("/{id}")
    public ResponseEntity<?> getLesson(@PathVariable Long id) {
        return lessonRepository.findById(id)
                .map(this::toFrontendDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 將 DB Entity 轉成前端 MissionData 結構
    private Map<String, Object> toFrontendDto(Lesson lesson) {
        return Map.of(
                "id", lesson.getId(),
                "name", lesson.getName(),
                "description", lesson.getDescription() != null ? lesson.getDescription() : "",
                // 前端需要小寫的 type (video/scroll)，後端 DB 存的大寫或小寫都相容一下
                "type", lesson.getType() != null ? lesson.getType().toLowerCase() : "video",
                "content", toContentDtoList(lesson.getContents()),
                "reward", toRewardDto(lesson)
        );
    }

    private List<Map<String, Object>> toContentDtoList(List<LessonContent> contents) {
        if (contents == null) return Collections.emptyList();

        return contents.stream().map(content -> Map.<String, Object>of(
                "id", content.getId(),
                "type", content.getContentType() != null ? content.getContentType().toLowerCase() : "video",
                "url", content.getUrl() != null ? content.getUrl() : "",
                "content", content.getContent() != null ? content.getContent() : ""
        )).collect(Collectors.toList());
    }

    private Map<String, Object> toRewardDto(Lesson lesson) {
        if (lesson.getReward() == null) {
            return Map.of("exp", 0, "coin", 0);
        }
        // 假設您的 Reward Entity 有 getExp() 和 getCoin()
        // 如果沒有 getter，請根據您的 Reward 結構修改這裡
        return Map.of(
                "exp", lesson.getReward().getId() != null ? 10 : 0, // 暫時寫死或根據真實欄位
                "coin", lesson.getReward().getId() != null ? 10 : 0
        );
    }
}