package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.entity.Member;
import com.waterballsa.tutorial_platform.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class DevAuthController {

    private final MemberRepository memberRepository;
    private final com.waterballsa.tutorial_platform.service.JwtService jwtService;

    @org.springframework.beans.factory.annotation.Value("${app.enable-test-login:false}")
    private boolean enableTestLogin;

    /**
     * 開發專用登入 API (Mock Login)
     */
    @PostMapping("/dev-login")
    public ResponseEntity<Map<String, Object>> devLogin(@RequestParam String email, HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        boolean isLocal = "127.0.0.1".equals(remoteAddr) || "0:0:0:0:0:0:0:1".equals(remoteAddr) || "localhost".equals(request.getServerName());

        if (!enableTestLogin && !isLocal) {
            log.warn("Mock login denied for non-local request: {}. Email: {}", remoteAddr, email);
            return ResponseEntity.status(403).body(Map.of("message", "Test login is disabled on production."));
        }
        // 1. 根據 Email 找使用者
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        // 2. 偽造 OAuth2User 資料 (保持不變)
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", member.getEmail());
        attributes.put("name", member.getName());
        attributes.put("picture", member.getAvatar());
        attributes.put("sub", "dev-user-" + member.getId());

        OAuth2User oauth2User = new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "email"
        );

        // 3. 建立 Authentication Token (保持不變)
        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
                oauth2User,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                "google"
        );

        // 4. 設定 SecurityContext (保持不變)
        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(authentication);

        // 5. 存入 Session (保持不變)
        HttpSession session = request.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", sc);

        // 6. ★ 生成 Token (已通過 DataMigrationPatch 補強資料，不再需要手動 null check)
        // String roleStr = (member.getRole() != null) ? member.getRole().name() : "ROLE_USER"; 
        String token = jwtService.generateToken(authentication, member.getEmail(), member.getRole().name());

        // 7. 回傳 MemberDTO + Token
        com.waterballsa.tutorial_platform.dto.MemberDTO dto = com.waterballsa.tutorial_platform.dto.MemberDTO.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .nickName(member.getNickName())
                .jobTitle(member.getJobTitle())
                .occupation(member.getOccupation())
                .level(member.getLevel())
                .exp(member.getExp())
                .nextLevelExp(member.getNextLevelExp())
                .avatar(member.getAvatar())
                .pictureUrl(member.getAvatar())
                .region(member.getRegion())
                .githubUrl(member.getGithubUrl())
                .discordId(member.getDiscordId())
                .role(member.getRole() != null ? member.getRole().name() : null)
                .instructorBio(member.getInstructorBio())
                .socialLinks(member.getSocialLinks())
                .build();

        Map<String, Object> result = new HashMap<>();
        result.put("user", dto);
        result.put("token", token);
        
        return ResponseEntity.ok(result);
    }
}