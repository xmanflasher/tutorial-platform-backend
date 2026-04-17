package com.waterballsa.tutorial_platform.service;

import com.waterballsa.tutorial_platform.entity.Notification;
import com.waterballsa.tutorial_platform.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 通知系統服務 [Ref: ISSUE-ARCH-09]
 * 負責處理各類系統通知的發送邏輯
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * 發送歡迎通知給新註冊用戶
     */
    @Transactional
    public void sendWelcomeNotification(Long memberId) {
        Notification notification = Notification.builder()
                .memberId(memberId)
                .message("👋 歡迎來到 Codeatl！準備好開始您的硬核學習旅程了嗎？")
                .linkText("看看課程")
                .linkHref("/courses")
                .build();
        notificationRepository.save(notification);
    }

    /**
     * 發送訂單建立通知
     */
    @Transactional
    public void sendOrderCreatedNotification(Long memberId, String orderNumber) {
        sendNotification(memberId, 
            "🎉 您的訂單 " + orderNumber + " 已建立成功！請前往完成支付。", 
            "查看訂單", 
            "/courses");
    }

    /**
     * 發送訂單支付成功通知
     */
    @Transactional
    public void sendOrderPaidNotification(Long memberId, String orderNumber, String journeySlug) {
        sendNotification(memberId, 
            "✅ 您的訂單 " + orderNumber + " 已完成支付！快去開始您的學習旅程吧。", 
            "前往挑戰地圖", 
            "/journeys/" + journeySlug);
    }

    /**
     * 發送通用通知
     */
    @Transactional
    public void sendNotification(Long memberId, String message, String linkText, String linkHref) {
        Notification notification = Notification.builder()
                .memberId(memberId)
                .message(message)
                .linkText(linkText)
                .linkHref(linkHref)
                .build();
        notificationRepository.save(notification);
    }
}
