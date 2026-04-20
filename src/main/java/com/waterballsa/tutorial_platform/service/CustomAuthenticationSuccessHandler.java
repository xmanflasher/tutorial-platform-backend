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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final MemberRepository memberRepo;
    private final NotificationService notificationService;
    private final JwtService jwtService;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final RestTemplate restTemplate = new RestTemplate();

    @org.springframework.beans.factory.annotation.Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        final String registrationId = oauthToken.getAuthorizedClientRegistrationId();
        OAuth2User oauthUser = oauthToken.getPrincipal();

        // ★ 屬性提取 (使用 final 確保 Lambda 安全性)
        final String email = extractEmail(registrationId, oauthUser, oauthToken);
        
        String tempName = oauthUser.getAttribute("name");
        if (tempName == null) {
            tempName = oauthUser.getAttribute("login"); // GitHub fallback
        }
        final String name = tempName;
        
        final String avatar = extractAvatar(registrationId, oauthUser);

        if (email == null || email.isEmpty()) {
            log.error("Failed to extract email from provider: {}. User data: {}", registrationId, oauthUser.getAttributes());
            response.sendRedirect(frontendUrl + "/?error=oauth2_failure&message=email_not_provided");
            return;
        }

        Member member = memberRepo.findByEmail(email).map(existingMember -> {
            // ★ 更新顯示資訊 (同步當前 Provider 資訊)
            if (name != null) {
                existingMember.setName(name);
            }
            if (avatar != null) {
                existingMember.setAvatar(avatar);
            }
            return memberRepo.save(existingMember);
        }).orElseGet(() -> {
            // ★ Lambda 引用變數必須是 final 或 effectively final
            Member newMember = memberRepo.save(Member.builder()
                    .email(email)
                    .name(name != null ? name : "User_" + registrationId)
                    .avatar(avatar)
                    .role(Member.Role.ROLE_USER)
                    .build());
            
            notificationService.sendWelcomeNotification(newMember.getId());
            return newMember;
        });

        // ★ 訪客追蹤
        syncVisitorId(request, member);

        String redirectPath = "/";
        if (request.getParameter("redirect") != null) {
            redirectPath = request.getParameter("redirect");
        }

        String token = jwtService.generateToken(authentication, email, member.getRole().name());
        log.info("Successfully authenticated user: {} via {}", email, registrationId);
        
        String finalRedirectUrl = frontendUrl + redirectPath + (redirectPath.contains("?") ? "&" : "?") + "token=" + token;
        response.sendRedirect(finalRedirectUrl);
    }

    private String extractEmail(String registrationId, OAuth2User user, OAuth2AuthenticationToken oauthToken) {
        String email = user.getAttribute("email");
        if (email == null && "github".equals(registrationId)) {
            email = fetchGithubEmail(oauthToken);
        }
        return email;
    }

    private String fetchGithubEmail(OAuth2AuthenticationToken oauthToken) {
        try {
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(), oauthToken.getName());
            
            if (client == null || client.getAccessToken() == null) return null;
            
            String tokenValue = client.getAccessToken().getTokenValue();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(tokenValue);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 使用 ParameterizedTypeReference 解決 unchecked 警告
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    "https://api.github.com/user/emails",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> emails = response.getBody();
                return emails.stream()
                        .filter(e -> Boolean.TRUE.equals(e.get("primary")))
                        .map(e -> (String) e.get("email"))
                        .findFirst()
                        .orElse(null);
            }
        } catch (Exception e) {
            log.error("Error fetching GitHub private email: {}", e.getMessage());
        }
        return null;
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