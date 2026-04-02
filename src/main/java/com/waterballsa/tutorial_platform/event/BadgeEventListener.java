package com.waterballsa.tutorial_platform.event;

import com.waterballsa.tutorial_platform.entity.GymBadge;
import com.waterballsa.tutorial_platform.entity.MemberBadge;
import com.waterballsa.tutorial_platform.entity.Notification;
import com.waterballsa.tutorial_platform.repository.GymBadgeRepository;
import com.waterballsa.tutorial_platform.repository.MemberBadgeRepository;
import com.waterballsa.tutorial_platform.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BadgeEventListener {

    private final GymBadgeRepository gymBadgeRepository;
    private final MemberBadgeRepository memberBadgeRepository;
    private final NotificationRepository notificationRepository;

    @EventListener
    @Transactional
    public void onGymPassed(GymPassedEvent event) {
        log.info("🏆 收到道館通關事件: Member={}, Gym={}", event.getMemberId(), event.getGymId());

        // 1. 尋找所有解鎖條件包含此 Gym 的徽章
        // 格式如: "GYM_COMPLETED:6000"
        String condition = "GYM_COMPLETED:" + event.getGymId();
        List<GymBadge> eligibleBadges = gymBadgeRepository.findAll().stream()
                .filter(b -> condition.equals(b.getUnlockCondition()))
                .toList();

        if (eligibleBadges.isEmpty()) {
            return;
        }

        for (GymBadge badge : eligibleBadges) {
            // 2. 檢查使用者是否已經擁有該徽章
            boolean alreadyHas = memberBadgeRepository.existsByMemberIdAndBadgeId(
                    event.getMemberId(), badge.getId());

            if (!alreadyHas) {
                // 3. 發放徽章
                MemberBadge memberBadge = MemberBadge.builder()
                        .memberId(event.getMemberId())
                        .badgeId(badge.getId())
                        .awardedAt(java.time.LocalDateTime.now())
                        .build();
                
                memberBadgeRepository.save(memberBadge);
                log.info("✨ 徽章自動核發成功: {} -> {}", event.getMemberId(), badge.getName());

                // 4. 發送個人通知 (ISSUE-NTF-01)
                notificationRepository.save(Notification.builder()
                        .memberId(event.getMemberId())
                        .message("恭喜獲得新徽章：【" + badge.getName() + "】！")
                        .linkText("立即查看")
                        .linkHref("/users/me/gym-badges")
                        .build());
            }
        }
    }
}
