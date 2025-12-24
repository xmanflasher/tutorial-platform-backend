package com.waterballsa.tutorial_platform.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waterballsa.tutorial_platform.entity.*;
import com.waterballsa.tutorial_platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final MemberRepository memberRepository;
    private final GymRepository gymRepository;
    private final MissionRepository missionRepository;
    private final MemberMissionRepository memberMissionRepository;
    private final GymSubmissionRepository gymSubmissionRepository;

    @Bean
    CommandLineRunner initDatabase(JourneyRepository journeyRepository) {
        return args -> {
            if (journeyRepository.count() == 0) {
                System.out.println("🚀 [1/3] 開始匯入 Journey JSON ...");
                ObjectMapper mapper = new ObjectMapper();
                // 忽略 JSON 中有但 Entity 沒有的欄位，避免報錯
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                try {
                    InputStream inputStream = new ClassPathResource("data.json").getInputStream(); // 確認路徑是否為 data.json 或 data/journeys.json
                    List<Journey> journeys = mapper.readValue(inputStream, new TypeReference<List<Journey>>() {});

                    // ▼▼▼▼▼ 修正後的邏輯 ▼▼▼▼▼
                    journeys.forEach(journey -> {
                        journey.setId(null); // 清除 Journey ID

                        // 1. 處理 Skills
                        if (journey.getSkills() != null) {
                            journey.getSkills().forEach(skill -> {
                                skill.setId(null);
                                skill.setJourney(journey); // ★ 建議打開這行，確保 skills table 的 journey_id 有值
                            });
                        }

// 2. 處理 Missions
                        if (journey.getMissions() != null) {
                            journey.getMissions().forEach(mission -> {
                                mission.setId(null);
                                mission.setJourney(journey);

                                // (A) 處理 Prerequisites (前置條件)
                                if (mission.getPrerequisites() != null) {
                                    mission.getPrerequisites().forEach(req -> {
                                        req.setId(null);
                                        req.setMission(mission);
                                        req.setCategory("PREREQUISITE"); // ★ 手動補上類別

                                        // 因為用了 @JsonAnySetter，現在 req.getParams() 裡面應該已經有 gymId 等資料了
                                    });
                                }

                                // (B) 處理 Criteria (驗收標準)
                                if (mission.getCriteria() != null) {
                                    mission.getCriteria().forEach(req -> {
                                        req.setId(null);
                                        req.setMission(mission);
                                        req.setCategory("CRITERIA"); // ★ 手動補上類別
                                    });
                                }
                            });
                        }

                        // 3. 處理 Chapters
                        if (journey.getChapters() != null) {
                            journey.getChapters().forEach(chapter -> {
                                chapter.setId(null);
                                chapter.setJourney(journey);

                                // 處理 Lessons
                                if (chapter.getLessons() != null) {
                                    chapter.getLessons().forEach(lesson -> {
                                        lesson.setId(null);
                                        lesson.setChapter(chapter);
                                    });
                                }

                                // 處理 Gyms
                                if (chapter.getGyms() != null) {
                                    chapter.getGyms().forEach(gym -> {
                                        gym.setId(null);
                                        gym.setChapter(chapter);

                                        // 處理 Challenges
                                        if (gym.getChallenges() != null) {
                                            gym.getChallenges().forEach(challenge -> {
                                                challenge.setId(null);
                                                challenge.setGym(gym); // ★ 建議補上這行，確保 challenge 的 gym_id 正確
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                    // ▲▲▲▲▲ 修正結束 ▲▲▲▲▲

                    journeyRepository.saveAll(journeys);
                    System.out.println("✅ Journey JSON 匯入成功！");

                } catch (Exception e) {
                    System.err.println("❌ Journey 匯入失敗: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("ℹ️ Journey 資料已存在，跳過匯入。");
            }

            System.out.println("🎉 所有資料初始化完成！");
        };
    }
}