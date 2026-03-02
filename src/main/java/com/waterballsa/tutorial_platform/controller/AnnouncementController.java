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
    public Map<String, Object> getLatestAnnouncement(Authentication authentication) {
        Long userId = memberService.getCurrentMemberId(authentication);
        return announcementRepository.findFirstByUserIdOrUserIdIsNullOrderByCreatedAtDesc(userId)
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
                    announcement.put("message", "將軟體設計精通之旅體驗課程的全部影片看完就可以獲得 3000 元課程折價券！");
                    announcement.put("linkText", "前往");
                    announcement.put("linkHref", "/journeys/software-design-pattern/chapters/23/lessons/162");
                    return announcement;
                });
    }

    @GetMapping
    public List<Map<String, Object>> getAnnouncements(Authentication authentication) {
        Long userId = memberService.getCurrentMemberId(authentication);
        return announcementRepository.findByUserIdOrUserIdIsNullOrderByCreatedAtDesc(userId)
                .stream()
                .map(a -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", a.getId());
                    map.put("message", a.getMessage());
                    map.put("linkText", a.getLinkText());
                    map.put("linkHref", a.getLinkHref());
                    map.put("createdAt", a.getCreatedAt());
                    map.put("isGlobal", a.getUserId() == null);
                    return map;
                })
                .collect(Collectors.toList());
    }

    @PostMapping
    public void createAnnouncement(@RequestBody Map<String, Object> request, Authentication authentication) {
        // If userId is provided in body, use it. Otherwise use current user.
        // If "global": true is provided, userId will stay null.
        Long targetUserId = null;
        if (!Boolean.TRUE.equals(request.get("global"))) {
            targetUserId = request.containsKey("userId") 
                ? (request.get("userId") != null ? Long.valueOf(request.get("userId").toString()) : null)
                : memberService.getCurrentMemberId(authentication);
        }

        announcementRepository.save(com.waterballsa.tutorial_platform.entity.Announcement.builder()
                .userId(targetUserId)
                .message(request.get("message").toString())
                .linkText(request.get("linkText") != null ? request.get("linkText").toString() : null)
                .linkHref(request.get("linkHref") != null ? request.get("linkHref").toString() : null)
                .build());
    }
}
