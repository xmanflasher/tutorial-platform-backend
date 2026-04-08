package com.waterballsa.tutorial_platform.service;

import com.waterballsa.tutorial_platform.entity.Member;
import com.waterballsa.tutorial_platform.repository.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final MemberRepository memberRepo;
    private final JwtService jwtService;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        String avatar = oauthUser.getAttribute("picture");

        Member member = memberRepo.findByEmail(email).map(existingMember -> {
            if ((existingMember.getAvatar() == null || existingMember.getAvatar().isEmpty()) && avatar != null) {
                existingMember.setAvatar(avatar);
                return memberRepo.save(existingMember);
            }
            return existingMember;
        }).orElseGet(() -> {
            return memberRepo.save(Member.builder()
                    .email(email)
                    .name(name)
                    .avatar(avatar)
                    .build());
        });

        // ★ 訪客追蹤鈎稽 (Visitor Hooking) - 改用 Cookie
        String visitorId = null;
        if (request.getCookies() != null) {
            visitorId = Arrays.stream(request.getCookies())
                    .filter(c -> "visitor_id".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        if (visitorId != null && !visitorId.isEmpty()) {
            member.setOriginVisitorId(visitorId);
            memberRepo.save(member);
        }

        String redirectUrl = "/";
        if (request.getParameter("redirect") != null) {
            redirectUrl = request.getParameter("redirect");
        }

        // ★ 生成 JWT 並帶入重定向 URL，加入 RBAC Role
        // 已通過 DataMigrationPatch 補強，回歸標準調用。
        // String roleStr = (member.getRole() != null) ? member.getRole().name() : "ROLE_USER";
        String token = jwtService.generateToken(authentication, email, member.getRole().name());
        
        // 從環境變數讀取前端網址，若無則預設為 localhost:3000
        String frontendUrl = System.getenv("FRONTEND_URL");
        if (frontendUrl == null || frontendUrl.isEmpty()) {
            frontendUrl = "http://localhost:3000";
        }
        
        String finalRedirectUrl = frontendUrl + redirectUrl + (redirectUrl.contains("?") ? "&" : "?") + "token=" + token;
        response.sendRedirect(finalRedirectUrl);
    }
}