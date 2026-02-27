package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.service.MemberService;
import com.waterballsa.tutorial_platform.service.MissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;
    private final MemberService memberService;

    // 接受任務
    @PostMapping("/{missionId}/accept")
    public ResponseEntity<Void> acceptMission(@PathVariable Long missionId, Authentication auth) {
        Long memberId = memberService.getCurrentMemberId(auth);
        if (memberId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        missionService.acceptMission(memberId, missionId);
        return ResponseEntity.noContent().build();
    }

    // 延長期限
    @PostMapping("/{missionId}/extend")
    public ResponseEntity<Void> extendMission(@PathVariable Long missionId, Authentication auth) {
        Long memberId = memberService.getCurrentMemberId(auth);
        if (memberId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        missionService.extendMissionDeadline(memberId, missionId);
        return ResponseEntity.noContent().build();
    }

    // 領取獎勵
    @PostMapping("/{missionId}/claim")
    public ResponseEntity<Void> claimReward(@PathVariable Long missionId, Authentication auth) {
        Long memberId = memberService.getCurrentMemberId(auth);
        if (memberId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        missionService.claimReward(memberId, missionId);
        return ResponseEntity.noContent().build();
    }
}