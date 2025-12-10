package com.waterballsa.tutorial_platform.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waterballsa.tutorial_platform.entity.*;
import com.waterballsa.tutorial_platform.repository.*;
import lombok.RequiredArgsConstructor; // 記得加這個
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // 如果沒加密可暫時拿掉

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor // 自動注入 Repositories
public class DataSeeder {

    private final MemberRepository memberRepository;
    private final GymRepository gymRepository;
    private final MissionRepository missionRepository;
    private final MemberMissionRepository memberMissionRepository;
    private final GymSubmissionRepository gymSubmissionRepository;

    @Bean
    CommandLineRunner initDatabase(JourneyRepository journeyRepository) {
        return args -> {
            // -------------------------------------------------------
            // Part 1: 匯入 Journey (原本的邏輯)
            // -------------------------------------------------------
            if (journeyRepository.count() == 0) {
                System.out.println("🚀 [1/3] 開始匯入 Journey JSON ...");
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                try {
                    InputStream inputStream = new ClassPathResource("data.json").getInputStream();
                    List<Journey> journeys = mapper.readValue(inputStream, new TypeReference<List<Journey>>() {});

                    // ... (省略原本繁瑣的關聯設定邏輯，保持你原本的代碼即可，或者貼上之前給你的完整版) ...
                    // 為了版面整潔，這裡假設你保留了之前關於 Journey 的處理邏輯

                    // 簡單處理示範 (若你用之前給的完整版，請保留那段，不要刪掉)
                    for (Journey j : journeys) {
                        j.setId(null);
                        if(j.getChapters() != null) j.getChapters().forEach(c -> {c.setJourney(j); c.setId(null);});
                    }
                    journeyRepository.saveAll(journeys);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // -------------------------------------------------------
            // Part 2: 建立會員 (Member) - 用於排行榜與登入
            // -------------------------------------------------------
            if (memberRepository.count() == 0) {
                System.out.println("🚀 [2/3] 建立假會員資料 ...");

                // 1. 建立你自己 (管理員/主角)
                Member me = Member.builder()
                        .name("再一次就掛機")
                        .email("xmanflasher@gmail.com") // ★ 這是你 Controller 寫死的 Email
                        .avatar("https://api.dicebear.com/7.x/avataaars/svg?seed=Felix")
                        .jobTitle("全端工程師")
                        .level(19)
                        .exp(31040L)
                        .coin(500L)
                        .build();
                memberRepository.save(me);

                // 2. 建立一些排行榜上的路人
                memberRepository.save(Member.builder().name("Elliot").email("elliot@test.com").avatar("https://api.dicebear.com/7.x/avataaars/svg?seed=Elliot").jobTitle("初級工程師").level(19).exp(31040L).build());
                memberRepository.save(Member.builder().name("精靈Ken Lin").email("ken@test.com").avatar("https://api.dicebear.com/7.x/avataaars/svg?seed=Ken").jobTitle("資深工程師").level(18).exp(29130L).build());
                memberRepository.save(Member.builder().name("Clark Chen").email("clark@test.com").avatar("https://api.dicebear.com/7.x/avataaars/svg?seed=Clark").jobTitle("架構師").level(17).exp(27260L).build());
            }

            // -------------------------------------------------------
            // Part 3: 建立道館 (Gym) 與 任務 (Mission)
            // -------------------------------------------------------
            if (gymRepository.count() == 0) {
                System.out.println("🚀 [3/3] 建立道館與任務 ...");

                // 建立 3 個道館
                Gym gym1 = gymRepository.save(Gym.builder().name("行雲流水的設計底層思路").description("基礎觀念").displayOrder(1).maxStars(3).build());
                Gym gym2 = gymRepository.save(Gym.builder().name("Christopher Alexander：設計模式").description("歷史背景").displayOrder(2).maxStars(3).build());
                Gym gym3 = gymRepository.save(Gym.builder().name("掌握「樣板方法」：最基礎的控制反轉").description("實戰演練").displayOrder(3).maxStars(3).build());

                // 幫你自己 (Member ID=1) 提交一些紀錄 (讓挑戰地圖看起來有進度)
                Member me = memberRepository.findById(1L).orElse(null);
                if (me != null) {
                    // 通過第一關 (3顆星)
                    gymSubmissionRepository.save(GymSubmission.builder()
                            .member(me).gym(gym1)
                            .status(GymSubmission.SubmissionStatus.PASSED)
                            .grade(3).submittedAt(LocalDateTime.now()).build());

                    // 通過第二關 (2顆星)
                    gymSubmissionRepository.save(GymSubmission.builder()
                            .member(me).gym(gym2)
                            .status(GymSubmission.SubmissionStatus.PASSED)
                            .grade(2).submittedAt(LocalDateTime.now()).build());

                    // 第三關還沒過 (OPEN) -> 程式邏輯會自動判斷
                }

                // 建立一些任務
                missionRepository.save(Mission.builder()
                        .name("新手任務一").description("完成註冊並登入").durationDays(30)
                        .rewardType(Mission.RewardType.EXP).rewardValue(500)
                        .unlockCondition("none")
                        .build());

                missionRepository.save(Mission.builder()
                        .name("白段任務二").description("通過道館 3").durationDays(30)
                        .rewardType(Mission.RewardType.SUBSCRIPTION).rewardValue(30) // 延長 30 天
                        .unlockCondition("gym_pass:3")
                        .build());
            }

            System.out.println("🎉 所有資料初始化完成！");
        };
    }
}