package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.dto.*;
import com.waterballsa.tutorial_platform.entity.Journey;
import com.waterballsa.tutorial_platform.repository.JourneyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/journeys")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class JourneyProgressController {

    private final JourneyRepository journeyRepository;

    @GetMapping("/{slug}/progress")
    public ResponseEntity<JourneyProgressDTO> getUserProgress(
            @PathVariable String slug,
            @RequestParam Long userId
    ) {
        // 1. 從 DB 撈資料
        Journey journey = journeyRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Journey not found: " + slug));

        JourneyProgressDTO response = new JourneyProgressDTO();

        // 2. 映射現有欄位 (只用你 Entity 裡有的)
        response.setId(String.valueOf(journey.getId()));
        response.setSlug(journey.getSlug());

        // ★ Name 對應 Title
        response.setTitle(journey.getName());

        // ★ Description 有就放
        response.setDescription(journey.getDescription());

        // ★★★ 3. 處理 Menus (這是 Sidebar 顯示的關鍵) ★★★
        if (journey.getMenus() != null) {
            List<JourneyMenuDTO> menuDtos = journey.getMenus().stream()
                    // ...
                    .map(entityMenu -> new JourneyMenuDTO( // 改用 Lombok 的全參數建構子或 builder
                            entityMenu.getName(),
                            entityMenu.getHref(),
                            entityMenu.getIcon()
                    ))
                    .collect(Collectors.toList());

            response.setMenus(menuDtos);
        }

        // 4. 任務部分 (暫時維持 Mock，等你之後開發 MemberService)
        response.setMissions(List.of(
                MemberMissionDTO.builder()
                        .missionId(1L)
                        .name("新手任務一")
                        .status("AVAILABLE")
                        .currentProgress(0)
                        .maxOpportunityCards(2)
                        .build()
        ));

        return ResponseEntity.ok(response);
    }
}