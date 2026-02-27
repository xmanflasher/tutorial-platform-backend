package com.waterballsa.tutorial_platform.service;

import com.waterballsa.tutorial_platform.dto.MemberDTO;
import com.waterballsa.tutorial_platform.entity.Member;
import com.waterballsa.tutorial_platform.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    // 取得當前登入者 ID 的核心方法 (支援 JWT, OAuth2, 以及 Session)
    public Long getCurrentMemberId(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        String email = null;

        if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            email = ((org.springframework.security.oauth2.core.user.OAuth2User) principal).getAttribute("email");
        } else if (principal instanceof org.springframework.security.oauth2.jwt.Jwt) {
            email = ((org.springframework.security.oauth2.jwt.Jwt) principal).getClaim("email");
        } else if (principal instanceof String) {
            email = (String) principal;
        } else {
            email = authentication.getName();
        }

        if (email == null) return null;
        return memberRepository.findByEmail(email)
                .map(Member::getId)
                .orElse(null);
    }

    // 根據 Email 獲取使用者 (用於 /api/me)
    public MemberDTO getMemberByEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Member not found: " + email));

        return MemberDTO.builder()
                .id(member.getId())
                .name(member.getName())
                .nickName(member.getNickName())
                .jobTitle(member.getJobTitle())
                .occupation(member.getOccupation())
                .level(member.getLevel())
                .exp(member.getExp())
                .nextLevelExp(member.getNextLevelExp())
                .avatar(member.getAvatar())
                .email(member.getEmail())
                .sex(member.getSex())
                .birthDate(member.getBirthDate())
                .region(member.getRegion())
                .githubUrl(member.getGithubUrl())
                .discordId(member.getDiscordId())
                .build();
    }
}