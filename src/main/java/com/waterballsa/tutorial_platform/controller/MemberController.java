package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.dto.GymStatusDTO;
import com.waterballsa.tutorial_platform.dto.LeaderboardDTO;
import com.waterballsa.tutorial_platform.dto.MemberDTO;
import com.waterballsa.tutorial_platform.entity.Member;
import com.waterballsa.tutorial_platform.repository.MemberRepository;
import com.waterballsa.tutorial_platform.service.GymService;
import com.waterballsa.tutorial_platform.service.LeaderboardService;
import com.waterballsa.tutorial_platform.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final LeaderboardService leaderboardService;
    private final GymService gymService;

    // 1. 獲取當前登入者 (Private Profile)
    @GetMapping("/api/me")
    public MemberDTO getCurrentUser(Authentication authentication) {
        Long memberId = memberService.getCurrentMemberId(authentication);
        if (memberId == null) return null;
        
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null) return null;
        
        // 這裡回傳完整的 DTO (包含敏感資訊)
        return MemberDTO.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .nickName(member.getNickName())
                .jobTitle(member.getJobTitle())
                .occupation(member.getOccupation())
                .level(member.getLevel())
                .exp(member.getExp())
                .nextLevelExp(member.getNextLevelExp())
                .pictureUrl(member.getAvatar())
                .region(member.getRegion())
                .githubUrl(member.getGithubUrl())
                .discordId(member.getDiscordId())
                .build();
    }

    // 2. 獲取公開使用者資料
    @GetMapping("/api/users")
    public List<MemberDTO> getUsers(@RequestParam List<Long> ids) {
        return memberRepository.findByIdIn(ids)
                .stream()
                .map(this::toPublicDto)
                .collect(Collectors.toList());
    }

    // 2.1 更新使用者資料
    @PatchMapping("/api/users/{id}")
    public MemberDTO updateMember(@PathVariable Long id, @RequestBody MemberDTO dto) {
        System.out.println("Updating member: " + id + ", dto: " + dto);
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        
        if (dto.getNickName() != null) member.setNickName(dto.getNickName());
        if (dto.getJobTitle() != null) member.setJobTitle(dto.getJobTitle());
        if (dto.getOccupation() != null) member.setOccupation(dto.getOccupation());
        if (dto.getSex() != null) member.setSex(dto.getSex());
        if (dto.getBirthDate() != null) member.setBirthDate(dto.getBirthDate());
        if (dto.getRegion() != null) member.setRegion(dto.getRegion());
        if (dto.getGithubUrl() != null) member.setGithubUrl(dto.getGithubUrl());
        if (dto.getDiscordId() != null) member.setDiscordId(dto.getDiscordId());
        
        Member saved = memberRepository.save(member);
        return toPublicDto(saved);
    }

    // 3. 排行榜
    @GetMapping("/api/leaderboard")
    public List<LeaderboardDTO> getLeaderboard() {
        return leaderboardService.getLeaderboard();
    }

    // 4. 挑戰地圖狀態
    @GetMapping("/api/gyms")
    public List<GymStatusDTO> getGyms(Authentication auth) {
        Long currentMemberId = memberService.getCurrentMemberId(auth);
        return gymService.getGymMap(currentMemberId);
    }

    // 轉換為「公開」DTO 的方法
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
                .pictureUrl(entity.getAvatar())
                .region(entity.getRegion())
                .githubUrl(entity.getGithubUrl())
                .discordId(entity.getDiscordId())
                .build();
    }
}