package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.entity.Notification;
import com.waterballsa.tutorial_platform.repository.NotificationRepository;
import com.waterballsa.tutorial_platform.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 個人通知控制器 [Ref: ISSUE-NTF-01]
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final MemberService memberService;

    @GetMapping("/me")
    public List<Notification> getMyNotifications(Authentication authentication) {
        Long memberId = memberService.getCurrentMemberId(authentication);
        return notificationRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
    }

    @GetMapping("/me/unread-count")
    public Map<String, Long> getUnreadCount(Authentication authentication) {
        Long memberId = memberService.getCurrentMemberId(authentication);
        long count = notificationRepository.countByMemberIdAndIsReadFalse(memberId);
        return Map.of("count", count);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, Authentication authentication) {
        Long memberId = memberService.getCurrentMemberId(authentication);
        Notification notification = notificationRepository.findById(id).orElse(null);
        
        if (notification == null || !notification.getMemberId().equals(memberId)) {
            return ResponseEntity.notFound().build();
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/me/read-all")
    public ResponseEntity<?> markAllAsRead(Authentication authentication) {
        Long memberId = memberService.getCurrentMemberId(authentication);
        notificationRepository.markAllAsReadByMemberId(memberId);
        return ResponseEntity.ok().build();
    }
}
