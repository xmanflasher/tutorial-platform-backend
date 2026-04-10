package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.dto.JourneyDetailDTO;
import com.waterballsa.tutorial_platform.dto.MemberMissionDTO;
import com.waterballsa.tutorial_platform.service.JourneyService;
import com.waterballsa.tutorial_platform.service.MemberService;
import com.waterballsa.tutorial_platform.service.MissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/journeys")
@RequiredArgsConstructor
public class JourneyController {

    private final JourneyService journeyService;
    private final MissionService missionService;
    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<List<JourneyDetailDTO>> getAllJourneys() {
        return ResponseEntity.ok(journeyService.getAllJourneys());
    }

    // ★★★ 修正重點 ★★★
    // 1. 參數必須是 String slug (不能是 Long id)
    // 2. 呼叫 service.getJourneyBySlug(slug)
    @GetMapping("/{slug}")
    public ResponseEntity<JourneyDetailDTO> getJourneyBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(journeyService.getJourneyBySlug(slug));
    }

    // GET /api/journeys/{slug}/missions
    @GetMapping("/{slug}/missions")
    public List<MemberMissionDTO> getMissionsByJourney(
            @PathVariable String slug,
            org.springframework.security.core.Authentication auth
    ) {
        Long memberId = memberService.getCurrentMemberId(auth);

        if (memberId == null) {
            // 如果沒登入，回傳空清單或拋出異常均可，這裡選擇回傳空清單
            return java.util.Collections.emptyList();
        }

        return missionService.getMissionsByJourneySlug(memberId, slug);
    }
}