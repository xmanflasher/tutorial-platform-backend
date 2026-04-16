package com.waterballsa.tutorial_platform.service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.error("OAuth2 Authentication failed: {}", exception.getMessage());
        
        // 將具體錯誤訊息傳回前端，方便診斷 (生產環境建議僅開發期這樣做)
        String errorMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
        String redirectUrl = frontendUrl + "/?error=oauth2_failure&message=" + errorMessage;
        
        response.sendRedirect(redirectUrl);
    }
}
