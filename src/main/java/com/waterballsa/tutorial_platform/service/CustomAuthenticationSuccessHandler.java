package com.waterballsa.tutorial_platform.service;

import com.waterballsa.tutorial_platform.entity.Member;
import com.waterballsa.tutorial_platform.repository.MemberRepository;
import com.waterballsa.tutorial_platform.service.NotificationService;
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
    private final NotificationService notificationService;
    private final JwtService jwtService;

    @org.springframework.beans.factory.annotation.Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken = 
                (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) authentication;
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
        OAuth2User oauthUser = oauthToken.getPrincipal();

        // ★ 屬性提取器映射 (ISSUE-AUTH-07/08 修復重點)
        String email = extractEmail(registrationId, oauthUser);
        String name = oauthUser.getAttribute("name");
        String avatar = extractAvatar(registrationId, oauthUser);

        if (email == null || email.isEmpty()) {
            log.error("Failed to extract email from provider: {}. User data: {}", registrationId, oauthUser.getAttributes());
            response.sendRedirect(frontendUrl + "/?error=oauth2_failure&message=email_not_provided");
            return;
        }

        Member member = memberRepo.findByEmail(email).map(existingMember -> {
            // ★ 更新顯示資訊 (解決 ISSUE-AUTH-08): 每次登錄同步當前 Provider 的資訊
            existingMember.setName(name != null ? name : existingMember.getName());
            if (avatar != null) {
                existingMember.setAvatar(avatar);
            }
            return memberRepo.save(existingMember);
        }).orElseGet(() -> {
            Member newMember = memberRepo.save(Member.builder()
                    .email(email)
                    .name(name != null ? name : "User_" + registrationId)
                    .avatar(avatar)
                    .role(Member.Role.ROLE_USER)
                    .build());
            
            notificationService.sendWelcomeNotification(newMember.getId());
            return newMember;
        });

        // ★ 訪客追蹤 (維持原邏輯)
        syncVisitorId(request, member);

        String redirectUrl = "/";
        if (request.getParameter("redirect") != null) {
            redirectUrl = request.getParameter("redirect");
        }

        String token = jwtService.generateToken(authentication, email, member.getRole().name());
        log.info("Successfully authenticated user: {} via {}", email, registrationId);
        
        String finalRedirectUrl = frontendUrl + redirectUrl + (redirectUrl.contains("?") ? "&" : "?") + "token=" + token;
        response.sendRedirect(finalRedirectUrl);
    }

    private String extractEmail(String registrationId, OAuth2User user) {
        String email = user.getAttribute("email");
        if (email == null && "github".equals(registrationId)) {
            // 如果 GitHub Email 是私有的，嘗試抓取暫時代置 (可視需求調整)
            log.warn("GitHub email is private. Consider using account-name@github.local as fallback.");
        }
        return email;
    }

    private String extractAvatar(String registrationId, OAuth2User user) {
        if ("github".equals(registrationId)) return user.getAttribute("avatar_url");
        if ("discord".equals(registrationId)) {
            String id = user.getAttribute("id");
            String avatarHash = user.getAttribute("avatar");
            if (id != null && avatarHash != null) {
                return String.format("https://cdn.discordapp.com/avatars/%s/%s.png", id, avatarHash);
            }
        }
        return user.getAttribute("picture"); // Google 預設
    }

    private void syncVisitorId(HttpServletRequest request, Member member) {
        if (request.getCookies() != null) {
            Arrays.stream(request.getCookies())
                    .filter(c -> "visitor_id".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .ifPresent(vId -> {
                        member.setOriginVisitorId(vId);
                        memberRepo.save(member);
                    });
        }
    }
}