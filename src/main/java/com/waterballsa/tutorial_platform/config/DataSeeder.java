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
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    @Bean
    @Transactional // 加入 Transactional 確保資料一致性
    CommandLineRunner initDatabase(JourneyRepository journeyRepository) {
        return args -> {
            if (journeyRepository.count() == 0) {
                System.out.println("🚀 [1/3] 開始匯入 Journey JSON ...");
                ObjectMapper mapper = new ObjectMapper();
                // 忽略 JSON 中有但 Entity 沒有的欄位 (例如 unknown properties)
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                try {
                    // 確認你的檔案放在 src/main/resources/data.json
                    InputStream inputStream = new ClassPathResource("data.json").getInputStream();
                    List<Journey> journeys = mapper.readValue(inputStream, new TypeReference<List<Journey>>() {});

                    // 準備一個 Map 來暫存: 舊的 Lesson ID -> 也就是 JSON 裡的 ID -> 對應到 Lesson 物件
                    // 用於稍後 Gym 綁定 relatedLessons
                    Map<Long, Lesson> originalLessonIdMap = new HashMap<>();

                    journeys.forEach(journey -> {
                        journey.setId(null); // 清除 ID，讓 DB 自動生成

                        // 1. 處理 Skills (建立雙向關聯)
                        if (journey.getSkills() != null) {
                            journey.getSkills().forEach(skill -> {
                                skill.setId(null);
                                skill.setJourney(journey); // ★ 綁定 FK
                            });
                        }

                        // 2. 處理 Missions
                        if (journey.getMissions() != null) {
                            journey.getMissions().forEach(mission -> {
                                mission.setId(null);
                                mission.setJourney(journey); // ★ 綁定 FK

                                // (A) 處理 Prerequisites (前置條件)
                                if (mission.getPrerequisites() != null) {
                                    mission.getPrerequisites().forEach(req -> {
                                        req.setId(null);
                                        req.setMission(mission); // ★ 綁定 FK
                                        req.setCategory("PREREQUISITE"); // ★ 手動補上類別
                                    });
                                }

                                // (B) 處理 Criteria (驗收標準)
                                if (mission.getCriteria() != null) {
                                    mission.getCriteria().forEach(req -> {
                                        req.setId(null);
                                        req.setMission(mission); // ★ 綁定 FK
                                        req.setCategory("CRITERIA"); // ★ 手動補上類別
                                    });
                                }
                            });
                        }

                        // 3. 處理 Chapters & Lessons (重要：先處理 Lesson 才能讓 Gym 關聯)
                        if (journey.getChapters() != null) {
                            journey.getChapters().forEach(chapter -> {
                                chapter.setId(null);
                                chapter.setJourney(journey); // ★ 綁定 FK

                                // 處理 Lessons
                                if (chapter.getLessons() != null) {
                                    chapter.getLessons().forEach(lesson -> {
                                        Long oldId = lesson.getId(); // 暫存 JSON 裡的舊 ID
                                        if (oldId != null) {
                                            originalLessonIdMap.put(oldId, lesson);
                                        }

                                        lesson.setId(null);
                                        lesson.setChapter(chapter); // ★ 綁定 FK
                                    });
                                }
                            });

                            // 4. 處理 Gyms (必須在 Lesson 處理完後，因為 Gym 可能會參照 Lesson)
                            // 注意：這裡需要第二次遍歷 Chapters，或者確保邏輯順序
                            journey.getChapters().forEach(chapter -> {
                                if (chapter.getGyms() != null) {
                                    chapter.getGyms().forEach(gym -> {
                                        gym.setId(null);
                                        gym.setChapter(chapter); // ★ 綁定 FK

                                        // ★ 處理 Challenges
                                        if (gym.getChallenges() != null) {
                                            gym.getChallenges().forEach(challenge -> {
                                                challenge.setId(null);
                                                challenge.setGym(gym); // ★ 綁定 FK
                                            });
                                        }

                                        // ★★★ 處理 Gym 與 Lesson 的關聯 (relatedLessonIds) ★★★
                                        // 假設 Gym 有一個欄位 List<Long> relatedLessonIds 來自 JSON
                                        // 我們需要把它轉換成 List<Lesson> relatedLessons
                                        /* if (gym.getRelatedLessonIds() != null) {
                                            List<Lesson> lessons = gym.getRelatedLessonIds().stream()
                                                .map(originalLessonIdMap::get) // 用舊 ID 找回 Lesson 物件
                                                .filter(java.util.Objects::nonNull)
                                                .collect(Collectors.toList());
                                            gym.setRelatedLessons(lessons);
                                        }
                                        */
                                    });
                                }
                            });
                        }
                    });

                    // 一次性儲存整個 Journey 結構 (因為有 CascadeType.ALL，會自動儲存所有子物件)
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