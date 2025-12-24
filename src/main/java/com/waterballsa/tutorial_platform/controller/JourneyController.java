package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.dto.JourneyDetailDTO;
import com.waterballsa.tutorial_platform.service.JourneyService;
import com.waterballsa.tutorial_platform.service.MissionService;
import com.waterballsa.tutorial_platform.dto.MemberMissionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // 2. Import AuthPrincipal
import org.springframework.security.oauth2.jwt.Jwt; // 3. Import Jwt
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/journeys")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class JourneyController {

    private final JourneyService journeyService;

    private final MissionService missionService;

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
            @AuthenticationPrincipal Jwt jwt // 如果沒 Token，這裡會是 null
    ) {
        Long memberId;

        if (jwt != null) {
            // 有 Token，解析 Token 裡的 ID
            memberId = Long.parseLong(jwt.getSubject());
        } else {
            // ★★★ 懶人模式啟動 ★★★
            // 沒 Token？沒關係，我就當你是 ID = 1 的測試員
            // 這樣你前端完全不用帶 Header 也能跑！
            System.out.println("【開發模式】偵測到無 Token，預設使用 Member ID = 1");
            memberId = 1L;
        }

        return missionService.getMissionsByJourneySlug(memberId, slug);
    }
}