package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.dto.*;
import com.waterballsa.tutorial_platform.entity.*;
import com.waterballsa.tutorial_platform.repository.JourneyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/journeys")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class JourneyProgressController {

    private final JourneyRepository journeyRepository;

    @GetMapping("/{slug}/progress")
    public ResponseEntity<JourneyProgressDTO> getUserProgress(
            @PathVariable String slug,
            @RequestParam(required = false) Long userId // userId 設為非必填，方便測試
    ) {
        // 1. 從 DB 撈資料
        Journey journey = journeyRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Journey not found: " + slug));

        JourneyProgressDTO response = new JourneyProgressDTO();

        // 2. 基礎欄位映射
        response.setId(String.valueOf(journey.getId()));
        response.setSlug(journey.getSlug());
        response.setTitle(journey.getName());
        response.setDescription(journey.getDescription());

        // 3. 處理 Menus
        if (journey.getMenus() != null) {
            List<JourneyMenuDTO> menuDtos = journey.getMenus().stream()
                    .map(entityMenu -> JourneyMenuDTO.builder()
                            .name(entityMenu.getName())
                            .href(entityMenu.getHref())
                            .icon(entityMenu.getIcon())
                            .build())
                    .collect(Collectors.toList());
            response.setMenus(menuDtos);
        }

        // ★★★ 4. [新增] 處理 Chapters (前端分組需要) ★★★
        if (journey.getChapters() != null) {
            // 1. 先確保章節依照 displayOrder 排好序
            List<Chapter> sortedChapters = journey.getChapters().stream()
                    .sorted(Comparator.comparing(Chapter::getDisplayOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();

            List<ChapterDTO> chapterDTOs = new ArrayList<>();

            // 2. 使用迴圈搭配索引 (Index) 來判斷段位
            for (int i = 0; i < sortedChapters.size(); i++) {
                Chapter ch = sortedChapters.get(i);

                // ★ 核心邏輯：前 3 章 (0, 1, 2) 是白帶，剩下是黑帶
                // 你可以在這裡調整數字，例如改成 i < 4
                String belt = (i < 4) ? "WHITE" : "BLACK";

                chapterDTOs.add(ChapterDTO.builder()
                        .id(String.valueOf(ch.getId()))
                        .name(ch.getName())
                        .belt(belt) // ★ 填入計算好的段位
                        .build());
            }

            response.setChapters(chapterDTOs);
        } else {
            response.setChapters(Collections.emptyList());
        }

        // ★★★ 5. [新增] 處理 Gyms (前端畫圖核心) ★★★
        if (journey.getGyms() != null) {
            List<GymDTO> gymDTOs = journey.getGyms().stream()
                    .sorted(Comparator.comparing(Gym::getId))
                    .map(gym -> GymDTO.builder()
                            .id(gym.getId())
                            .name(gym.getName())
                            .description(gym.getDescription())

                            // ★ 關鍵：前端顯示編號 "4.A" 需要這個
                            .code(gym.getCode())

                            // ★ 關鍵：前端分組 filter 需要這個
                            .chapterId(gym.getChapter() != null ? gym.getChapter().getId() : null)
                            .maxStars(gym.getMaxStars())
                            .type(gym.getType() != null ? gym.getType().name() : "CHALLENGE")
                            .difficulty(gym.getDifficulty())
                            .rewardExp(gym.getRewardExp())
                            .build())
                    .collect(Collectors.toList());
            response.setGyms(gymDTOs);
        } else {
            response.setGyms(Collections.emptyList());
        }

        // 6. 任務部分 (Mock)
        response.setMissions(List.of(
                MemberMissionDTO.builder()
                        .missionId(1L)
                        .name("新手任務一")
                        .status("AVAILABLE")
                        .currentProgress(0)
                        .maxOpportunityCards(2)
                        .build()
        ));

        // 7. 等級部分 (Mock 或從 MemberService 撈)
        response.setLevel(1);
        response.setCurrentExp(0);
        response.setMaxExp(100);

        return ResponseEntity.ok(response);
    }
}