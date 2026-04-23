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
@RequestMapping("/announcements")
@RequiredArgsConstructor
public class AnnouncementController {
    private final com.waterballsa.tutorial_platform.repository.AnnouncementRepository announcementRepository;
    private final MemberService memberService;

    @GetMapping("/latest")
    public Map<String, Object> getLatestAnnouncement() {
        try {
            return announcementRepository.findFirstByOrderByCreatedAtDesc()
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
                        announcement.put("message", "觀看課程、挑戰道館，導師親授助你火速成就大師實力！");
                        announcement.put("linkText", "前往課程");
                        announcement.put("linkHref", "/journeys/javascript-basics-140");
                        return announcement;
                    });
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "DEBUG_ERROR_LATEST: " + e.getMessage());
            errorMap.put("id", -1);
            return errorMap;
        }
    }

    @GetMapping
    public List<Map<String, Object>> getAnnouncements() {
        try {
            return announcementRepository.findAll().stream()
                    .sorted((a1, a2) -> {
                        java.time.LocalDateTime t1 = a1.getCreatedAt() != null ? a1.getCreatedAt() : java.time.LocalDateTime.MIN;
                        java.time.LocalDateTime t2 = a2.getCreatedAt() != null ? a2.getCreatedAt() : java.time.LocalDateTime.MIN;
                        return t2.compareTo(t1);
                    })
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
        } catch (Exception e) {
            return List.of(Map.of("error", "DEBUG_ERROR_LIST: " + e.getMessage()));
        }
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
