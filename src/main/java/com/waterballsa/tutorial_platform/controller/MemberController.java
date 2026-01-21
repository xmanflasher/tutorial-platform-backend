package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.dto.GymStatusDTO;
import com.waterballsa.tutorial_platform.dto.LeaderboardDTO;
import com.waterballsa.tutorial_platform.dto.MemberDTO;
import com.waterballsa.tutorial_platform.entity.Member; // 暫時直接依賴 Entity 轉 DTO (簡單做法)
import com.waterballsa.tutorial_platform.repository.MemberRepository; // 暫時直接呼叫 Repo
import com.waterballsa.tutorial_platform.service.GymService;
import com.waterballsa.tutorial_platform.service.LeaderboardService;
import com.waterballsa.tutorial_platform.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
// @CrossOrigin(origins = "http://localhost:3000") // 已經在 SecurityConfig 設定過了，這裡可以不用
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository; // ★ 注入 Repo (正規應寫在 Service，這邊方便示範)
    private final LeaderboardService leaderboardService;
    private final GymService gymService;

    // 1. 獲取當前登入者 (Private Profile)
    @GetMapping("/api/me")
    public MemberDTO getCurrentUser() {
        String email = "xmanflasher@gmail.com";
        return memberService.getMemberByEmail(email);
    }

    // 2. ★★★ 新增：獲取公開使用者資料 (Public Portfolio Profile) ★★★
    // 對應前端 fetch: /api/users?ids=839
    @GetMapping("/api/users")
    public List<MemberDTO> getUsers(@RequestParam List<Long> ids) {
        return memberRepository.findByIdIn(ids)
                .stream()
                .map(this::toPublicDto) // 使用下方的轉換方法
                .collect(Collectors.toList());
    }

    // 3. 排行榜
    @GetMapping("/api/leaderboard")
    public List<LeaderboardDTO> getLeaderboard() {
        return leaderboardService.getLeaderboard();
    }

    // 4. 挑戰地圖狀態
    @GetMapping("/api/gyms")
    public List<GymStatusDTO> getGyms() {
        Long currentMemberId = 1L;
        return gymService.getGymMap(currentMemberId);
    }

    // ★ 轉換為「公開」DTO 的方法 (不回傳 email)
    private MemberDTO toPublicDto(Member entity) {
        return MemberDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .nickName(entity.getNickName())
                .jobTitle(entity.getJobTitle())
                .occupation(entity.getOccupation())
                .level(entity.getLevel())
                .exp(entity.getExp())
                .nextLevelExp(entity.getNextLevelExp())
                .pictureUrl(entity.getAvatar()) // ★ 將 avatar 轉給 pictureUrl
                // .email(entity.getEmail())    // ★ 故意註解掉：保護隱私
                .build();
    }
}