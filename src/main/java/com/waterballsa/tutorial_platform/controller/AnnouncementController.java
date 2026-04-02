package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {
    private final com.waterballsa.tutorial_platform.repository.AnnouncementRepository announcementRepository;
    private final MemberService memberService;

    @GetMapping("/latest")
    public Map<String, Object> getLatestAnnouncement() {
        return announcementRepository.findAll().stream()
                .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                .findFirst()
                .map(a -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", a.getId());
                    map.put("message", a.getMessage());
                    map.put("linkText", a.getLinkText());
                    map.put("linkHref", a.getLinkHref());
                    return map;
                })
                .orElseGet(() -> {
                    Map<String, Object> announcement = new HashMap<>();
                    announcement.put("id", 1);
                    announcement.put("message", "歡迎來到軟體設計精通之旅！");
                    announcement.put("linkText", "查看");
                    announcement.put("linkHref", "/");
                    return announcement;
                });
    }

    @GetMapping
    public List<Map<String, Object>> getAnnouncements() {
        return announcementRepository.findAll().stream()
                .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                .map(a -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", a.getId());
                    map.put("message", a.getMessage());
                    map.put("linkText", a.getLinkText());
                    map.put("linkHref", a.getLinkHref());
                    map.put("createdAt", a.getCreatedAt());
                    map.put("isGlobal", true);
                    return map;
                })
                .collect(Collectors.toList());
    }

    @PostMapping
    public void createAnnouncement(@RequestBody Map<String, Object> request) {
        announcementRepository.save(com.waterballsa.tutorial_platform.entity.Announcement.builder()
                .message(request.get("message").toString())
                .linkText(request.get("linkText") != null ? request.get("linkText").toString() : null)
                .linkHref(request.get("linkHref") != null ? request.get("linkHref").toString() : null)
                .build());
    }
}
