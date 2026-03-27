package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.dto.GymBadgeDTO;
import com.waterballsa.tutorial_platform.dto.GymDetailDTO;
import com.waterballsa.tutorial_platform.service.GymService;
import com.waterballsa.tutorial_platform.service.MemberService;
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
    private final MemberService memberService;

    @GetMapping("/journeys/{journeyId}/gym-badges")
    public ResponseEntity<List<GymBadgeDTO>> getGymBadges(
            @PathVariable Long journeyId,
            org.springframework.security.core.Authentication auth
    ) {
        Long userId = memberService.getCurrentMemberId(auth);
        return ResponseEntity.ok(gymService.getBadgesByJourney(userId, journeyId));
    }

    // 2. 取得特定道館的詳細資料 (給 GymDetailView 用)
    @GetMapping("/gyms/{id}")
    public ResponseEntity<GymDetailDTO> getGymDetail(@PathVariable Long id) {
        GymDetailDTO detail = gymService.getGymDetail(id);
        return detail != null ? ResponseEntity.ok(detail) : ResponseEntity.notFound().build();
    }
}