package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.entity.Member;
import com.waterballsa.tutorial_platform.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity; // 1. 記得引入這行
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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
// 2. 允許前端跨域 (重要)
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class DevAuthController {

    private final MemberRepository memberRepository;

    /**
     * 開發專用登入 API (Mock Login)
     */
    @PostMapping("/dev-login")
    // 3. 修改回傳型別 String -> ResponseEntity<Member>
    public ResponseEntity<Member> devLogin(@RequestParam String email, HttpServletRequest request) {
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

        // 6. ★★★ 關鍵修正 ★★★：回傳 JSON 物件，而不是字串
        return ResponseEntity.ok(member);
    }
}