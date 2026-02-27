package com.waterballsa.tutorial_platform.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @GetMapping("/latest")
    public Map<String, Object> getLatestAnnouncement() {
        Map<String, Object> announcement = new HashMap<>();
        announcement.put("id", 1);
        announcement.put("message", "歡迎來到水球軟體學院！這裡是您的學習起點，探索最新的軟體技術與實作練習。");
        announcement.put("linkText", "第一站：精進軟體設計");
        announcement.put("linkHref", "/journeys/software-design-pattern");
        return announcement;
    }
}
