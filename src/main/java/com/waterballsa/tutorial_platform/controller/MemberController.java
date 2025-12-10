package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.dto.GymStatusDTO;
import com.waterballsa.tutorial_platform.dto.LeaderboardDTO;
import com.waterballsa.tutorial_platform.dto.MemberDTO;
import com.waterballsa.tutorial_platform.service.GymService;
import com.waterballsa.tutorial_platform.service.LeaderboardService;
import com.waterballsa.tutorial_platform.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final LeaderboardService leaderboardService;
    private final GymService gymService;

    // 1. 獲取當前登入者資料 (用於 Header)
    @GetMapping("/api/me")
    public MemberDTO getCurrentUser(/* @AuthenticationPrincipal OAuth2User principal */) {
        // ★ 暫時寫死一個 Email 方便測試，等你 OAuth 好了再換回 principal.getAttribute("email")
        String email = "xmanflasher@gmail.com";
        return memberService.getMemberByEmail(email);
    }

    // 2. 排行榜
    @GetMapping("/api/leaderboard")
    public List<LeaderboardDTO> getLeaderboard() {
        return leaderboardService.getLeaderboard();
    }

    // 3. 挑戰地圖狀態
    @GetMapping("/api/gyms")
    public List<GymStatusDTO> getGyms(/* @AuthenticationPrincipal OAuth2User principal */) {
        // ★ 暫時寫死 ID=1 (你自己)，之後要從 DB 查出這個 User 的 ID
        Long currentMemberId = 1L;
        return gymService.getGymMap(currentMemberId);
    }
}