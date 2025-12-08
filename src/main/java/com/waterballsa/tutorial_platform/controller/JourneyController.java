package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.dto.JourneyDetailDTO;
import com.waterballsa.tutorial_platform.service.JourneyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/journeys")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class JourneyController {

    private final JourneyService journeyService;

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
}