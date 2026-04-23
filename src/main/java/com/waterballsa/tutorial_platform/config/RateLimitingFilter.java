package com.waterballsa.tutorial_platform.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RateLimitingFilter - 實施 API 請求速率限制
 * 使用 Bucket4j 實作權杖桶演算法 (Token Bucket)，以用戶 IP 為 Key。
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    @org.springframework.beans.factory.annotation.Value("${app.rate-limit.capacity:100}")
    private int capacity;

    @org.springframework.beans.factory.annotation.Value("${app.rate-limit.time-unit-minutes:1}")
    private int timeUnitMinutes;

    // 使用 ConcurrentHashMap 儲存每個 IP 對應的權杖桶
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(capacity, Refill.greedy(capacity, Duration.ofMinutes(timeUnitMinutes)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 僅對 API 進行速率限制
        if (request.getRequestURI().startsWith("/api/")) {
            String ip = getClientIp(request);
            
            // ★ 修復點 1: Local 環境 (localhost/127.0.0.1) 豁免限流
            if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "localhost".equals(ip)) {
                filterChain.doFilter(request, response);
                return;
            }

            Bucket bucket = buckets.computeIfAbsent(ip, k -> createNewBucket());

            if (bucket.tryConsume(1)) {
                filterChain.doFilter(request, response);
            } else {
                // ★ 修復點 2: 提供更具辨識度的 429 回應內容
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\": \"RATE_LIMIT_EXCEEDED\", \"error\": \"操作過於頻繁，請稍後再試。\"}");
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
