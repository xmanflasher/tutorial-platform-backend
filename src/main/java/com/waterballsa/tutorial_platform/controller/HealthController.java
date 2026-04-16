package com.waterballsa.tutorial_platform.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/**
 * 健康檢查接口 (Health Check API)
 * 用於前端狀態燈 (BackendStatusWatcher) 偵測伺服器是否在線。
 */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "message", "Σ-Codeatl Backend is running.");
    }
}
