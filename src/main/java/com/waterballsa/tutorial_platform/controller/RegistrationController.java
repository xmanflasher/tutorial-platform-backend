package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.entity.Member;
import com.waterballsa.tutorial_platform.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RegistrationController {

    private final MemberRepository memberRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationRequest request, jakarta.servlet.http.HttpServletRequest httpRequest) {
        if (memberRepository.findByEmail(request.email).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        Member member = Member.builder()
                .name(request.name)
                .email(request.email)
                .password(request.password)
                .occupation(request.role)
                .avatar("https://api.dicebear.com/7.x/miniavs/svg?seed=" + java.util.UUID.randomUUID())
                .level(1)
                .exp(0L)
                .nextLevelExp(2000L)
                .build();

        Member saved = memberRepository.save(member);
        loginUser(saved, httpRequest);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/quick-register")
    public ResponseEntity<?> quickRegister(@RequestBody(required = false) QuickRegistrationRequest request, jakarta.servlet.http.HttpServletRequest httpRequest) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        String role = (request != null && request.role != null) ? request.role : "其他";
        
        Member member = Member.builder()
                .name("冒險者_" + id)
                .email("guest_" + id + "@test.com")
                .password("pass123")
                .occupation(role)
                .avatar("https://api.dicebear.com/7.x/miniavs/svg?seed=" + id)
                .level(1)
                .exp(0L)
                .nextLevelExp(2000L)
                .build();

        Member saved = memberRepository.save(member);
        loginUser(saved, httpRequest);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) {
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        
        // 手動清除 Cookie
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("JSESSIONID", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        
        return ResponseEntity.ok().build();
    }

    private void loginUser(Member member, jakarta.servlet.http.HttpServletRequest request) {
        // 偽造 OAuth2User 資料 (與 DevAuthController 一致)
        java.util.Map<String, Object> attributes = new java.util.HashMap<>();
        attributes.put("email", member.getEmail());
        attributes.put("name", member.getName());
        attributes.put("picture", member.getAvatar());
        attributes.put("sub", "reg-user-" + member.getId());

        org.springframework.security.oauth2.core.user.OAuth2User oauth2User = new org.springframework.security.oauth2.core.user.DefaultOAuth2User(
                java.util.Collections.singleton(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "email"
        );

        org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken authentication = new org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken(
                oauth2User,
                java.util.Collections.singleton(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")),
                "google"
        );

        org.springframework.security.core.context.SecurityContext sc = org.springframework.security.core.context.SecurityContextHolder.getContext();
        sc.setAuthentication(authentication);

        jakarta.servlet.http.HttpSession session = request.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", sc);
    }

    public static class QuickRegistrationRequest {
        public String role;
    }

    public static class RegistrationRequest {
        public String name;
        public String email;
        public String password;
        public String role;
    }
}
