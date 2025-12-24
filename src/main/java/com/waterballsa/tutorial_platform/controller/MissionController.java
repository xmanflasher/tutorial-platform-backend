package com.waterballsa.tutorial_platform.controller;
import com.waterballsa.tutorial_platform.service.MissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    // 接受任務
    @PostMapping("/{missionId}/accept")
    public ResponseEntity<Void> acceptMission(@PathVariable Long missionId, @AuthenticationPrincipal Jwt jwt) {
        Long memberId = Long.parseLong(jwt.getSubject());
        missionService.acceptMission(memberId, missionId);
        return ResponseEntity.ok().build();
    }

    // 延長期限
    @PostMapping("/{missionId}/extend")
    public ResponseEntity<Void> extendMission(@PathVariable Long missionId, @AuthenticationPrincipal Jwt jwt) {
        Long memberId = Long.parseLong(jwt.getSubject());
        missionService.extendMissionDeadline(memberId, missionId);
        return ResponseEntity.ok().build();
    }

    // 領取獎勵
    @PostMapping("/{missionId}/claim")
    public ResponseEntity<Void> claimReward(@PathVariable Long missionId, @AuthenticationPrincipal Jwt jwt) {
        Long memberId = Long.parseLong(jwt.getSubject());
        missionService.claimReward(memberId, missionId);
        return ResponseEntity.ok().build();
    }
}