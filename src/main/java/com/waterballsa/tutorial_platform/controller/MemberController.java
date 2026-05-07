package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.dto.*;
import com.waterballsa.tutorial_platform.service.GymService;
import com.waterballsa.tutorial_platform.service.LeaderboardService;
import com.waterballsa.tutorial_platform.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final LeaderboardService leaderboardService;
    private final GymService gymService;
    private final com.waterballsa.tutorial_platform.service.SkillRatingService skillRatingService;

    // 1. 獲取當前登入者 (Private Profile)
    @GetMapping("/me")
    public MemberDTO getCurrentUser(Authentication authentication) {
        Long memberId = memberService.getCurrentMemberId(authentication);
        return memberService.getMemberDto(memberId);
    }

    // 1.0 獲取課程專屬技能統計 (Phase 29: ARCH-FIX-02)
    @GetMapping("/api/members/me/skill-stats")
    public List<SkillStatDTO> getSkillStats(Authentication auth, @RequestParam(required = false) Long journeyId) {
        Long memberId = memberService.getCurrentMemberId(auth);
        if (memberId == null) throw new RuntimeException("Unauthorized");
        return skillRatingService.getJourneySkillStats(memberId, journeyId);
    }

    // 1.1 更新當前登入者資料 (Phase 7)
    @PutMapping("/members/me")
    public MemberDTO updateCurrentMember(Authentication auth, @Valid @RequestBody MemberUpdateDTO updateDTO) {
        Long memberId = memberService.getCurrentMemberId(auth);
        if (memberId == null) throw new RuntimeException("Unauthorized");
        return memberService.updateMemberProfile(memberId, updateDTO);
    }

    // 1.2 更新講師版面 (Phase 9.1)
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @PutMapping("/users/me/instructor-profile")
    public MemberDTO updateInstructorProfile(Authentication auth, @Valid @RequestBody InstructorProfileUpdateDTO updateDTO) {
        Long memberId = memberService.getCurrentMemberId(auth);
        if (memberId == null) throw new RuntimeException("Unauthorized");
        return memberService.updateInstructorProfile(memberId, updateDTO);
    }

    // 2. 獲取公開使用者資料
    @GetMapping("/users")
    public List<MemberDTO> getUsers(@RequestParam List<Long> ids) {
        return memberService.getMembersByIds(ids);
    }

    // 2.1 更新使用者資料 (需要驗證權限)
    @PatchMapping("/users/{id}")
    public MemberDTO updateMember(Authentication auth, @PathVariable Long id, @Valid @RequestBody MemberDTO dto) {
        Long currentId = memberService.getCurrentMemberId(auth);
        
        // 權限檢查：只能修改自己的資料，除非是 ADMIN
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));
        
        if (currentId == null || (!currentId.equals(id) && !isAdmin)) {
            throw new RuntimeException("Unauthorized: You can only update your own profile.");
        }

        return memberService.updateMember(id, dto);
    }

    // 3. 排行榜
    @GetMapping("/leaderboard")
    public List<LeaderboardDTO> getLeaderboard() {
        return leaderboardService.getLeaderboard();
    }

    // 4. 挑戰地圖狀態
    @GetMapping("/gyms")
    public List<GymStatusDTO> getGyms(Authentication auth) {
        Long currentMemberId = memberService.getCurrentMemberId(auth);
        return gymService.getGymMap(currentMemberId);
    }
}