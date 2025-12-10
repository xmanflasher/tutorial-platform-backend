// CustomAuthenticationSuccessHandler.java
package com.waterballsa.tutorial_platform.service; // 或 config

import com.waterballsa.tutorial_platform.entity.Member;
import com.waterballsa.tutorial_platform.repository.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final MemberRepository memberRepo;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 從 Authentication 獲取 OAuth 使用者資料
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        String avatar = oauthUser.getAttribute("picture"); // Google/Facebook 可能用 "picture"

        // 檢查使用者是否已存在於我們的 DB
        Member member = memberRepo.findByEmail(email).orElseGet(() -> {
            // 如果不存在，建立新的 Member 紀錄
            return memberRepo.save(Member.builder()
                    .email(email)
                    .name(name)
                    .avatar(avatar)
                    .build());
        });

        // 處理登入後的導向邏輯 (導向前端首頁或原先想訪問的頁面)
        String redirectUrl = "/"; // 預設導回首頁
        if (request.getParameter("redirect") != null) {
            redirectUrl = request.getParameter("redirect");
        }

        // 最終導向前端的 URL (假設您的 Next.js 運行在 localhost:3000)
        response.sendRedirect("http://localhost:3000" + redirectUrl);
    }
}