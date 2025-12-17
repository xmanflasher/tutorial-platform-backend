package com.waterballsa.tutorial_platform;

import com.waterballsa.tutorial_platform.repository.GymRepository;
import com.waterballsa.tutorial_platform.repository.JourneyRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean; // 👈 新增這個 import
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository; // 👈 新增這個 import

@SpringBootTest
public class DataImportTest {

    // 👇 加入這段：告訴 Spring 在測試時，給一個假的 OAuth2 儲存庫，繞過設定檢查
    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private JourneyRepository journeyRepository;

    @Autowired
    private GymRepository gymRepository;

    @Test
    void testDataImportIntegrity() {
        // ... (原本的測試程式碼保持不變) ...
        long journeyCount = journeyRepository.count();
        System.out.println("📊 目前 Journey 總數: " + journeyCount);
        Assertions.assertTrue(journeyCount > 0, "❌ 錯誤：資料庫中沒有 Journey 資料！");

        long gymCount = gymRepository.count();
        System.out.println("📊 目前 Gym 總數: " + gymCount);
        Assertions.assertTrue(gymCount > 0, "❌ 錯誤：資料庫中沒有 Gym 資料！");
    }
}