package com.waterballsa.tutorial_platform.service;

import com.waterballsa.tutorial_platform.entity.Member;
import com.waterballsa.tutorial_platform.entity.Notification;
import com.waterballsa.tutorial_platform.repository.MemberRepository;
import com.waterballsa.tutorial_platform.repository.NotificationRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final MemberRepository memberRepo;
    private final NotificationRepository notificationRepo;
    private final JwtService jwtService;

    @org.springframework.beans.factory.annotation.Value("${app.frontend-url}")
    private String frontendUrl;

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
            Member newMember = memberRepo.save(Member.builder()
                    .email(email)
                    .name(name)
                    .avatar(avatar)
                    .build());
            
            // ★ 新帳號初始化 (ISSUE-BUG-06): 建立歡迎通知
            notificationRepo.save(Notification.builder()
                    .memberId(newMember.getId())
                    .message("👋 歡迎來到 Codeatl！準備好開始您的硬核之學習旅程了嗎？")
                    .linkText("看看課程")
                    .linkHref("/courses")
                    .build());
            
            return newMember;
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
        
        log.info("Successfully authenticated user: {}, Role: {}", email, member.getRole());
        
        String finalRedirectUrl = frontendUrl + redirectUrl + (redirectUrl.contains("?") ? "&" : "?") + "token=" + token;
        response.sendRedirect(finalRedirectUrl);
    }
}