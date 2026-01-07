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
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final LessonRepository lessonRepository; // 注入 LessonRepository 用來查關聯

    @Bean
    @Transactional
    CommandLineRunner initDatabase(JourneyRepository journeyRepository) {
        return args -> {
            if (journeyRepository.count() == 0) {
                System.out.println("🚀 [1/3] 開始匯入 Journey JSON ...");
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                try {
                    InputStream inputStream = new ClassPathResource("data.json").getInputStream();
                    List<Journey> journeys = mapper.readValue(inputStream, new TypeReference<List<Journey>>() {});

                    // ---------------------------------------------------------
                    // 第一階段：設定 Original ID 並清除 DB ID，建立層級關聯
                    // ---------------------------------------------------------
                    journeys.forEach(journey -> {
                        // 1. Journey
                        journey.setOriginalId(String.valueOf(journey.getId()));
                        journey.setId(null);

                        // 2. Skills
                        if (journey.getSkills() != null) {
                            journey.getSkills().forEach(skill -> {
                                skill.setOriginalId(String.valueOf(skill.getId()));
                                skill.setId(null);
                                skill.setJourney(journey);
                            });
                        }

                        // 3. Missions
                        if (journey.getMissions() != null) {
                            journey.getMissions().forEach(mission -> {
                                mission.setOriginalId(String.valueOf(mission.getId()));
                                mission.setId(null);
                                mission.setJourney(journey);

                                // Prerequisites
                                if (mission.getPrerequisites() != null) {
                                    mission.getPrerequisites().forEach(req -> {
                                        req.setOriginalId(String.valueOf(req.getId()));
                                        req.setId(null);
                                        req.setMission(mission);
                                        req.setCategory("PREREQUISITE");
                                    });
                                }
                                // Criteria
                                if (mission.getCriteria() != null) {
                                    mission.getCriteria().forEach(req -> {
                                        req.setOriginalId(String.valueOf(req.getId()));
                                        req.setId(null);
                                        req.setMission(mission);
                                        req.setCategory("CRITERIA");
                                    });
                                }
                            });
                        }

                        // 4. Chapters (Lessons & Gyms)
                        if (journey.getChapters() != null) {
                            journey.getChapters().forEach(chapter -> {
                                chapter.setOriginalId(String.valueOf(chapter.getId()));
                                chapter.setId(null);
                                chapter.setJourney(journey);

                                // Lessons
                                if (chapter.getLessons() != null) {
                                    chapter.getLessons().forEach(lesson -> {
                                        lesson.setOriginalId(String.valueOf(lesson.getId()));
                                        lesson.setId(null);
                                        lesson.setChapter(chapter);
                                    });
                                }

                                // Gyms (先只處理基本屬性與 Challenge，Lesson 關聯留到第二階段)
                                if (chapter.getGyms() != null) {
                                    chapter.getGyms().forEach(gym -> {
                                        gym.setOriginalId(String.valueOf(gym.getId()));
                                        gym.setId(null);
                                        gym.setChapter(chapter);

                                        if (gym.getChallenges() != null) {
                                            gym.getChallenges().forEach(challenge -> {
                                                challenge.setOriginalId(String.valueOf(challenge.getId()));
                                                challenge.setId(null);
                                                challenge.setGym(gym);
                                            });
                                        }
                                    });
                                }
                            });
                        }
                        // ★★★ [4] 補上 Menus 處理邏輯 (之前漏掉了這裡！) ★★★
                        if (journey.getMenus() != null) {
                            journey.getMenus().forEach(menu -> {
                                // menu 沒有 original_id 沒關係，主要是要清除 ID 並綁定 Parent
                                menu.setId(null);
                                menu.setJourney(journey); // 關鍵：綁定 Foreign Key
                            });
                        }

                    });

                    // ---------------------------------------------------------
                    // 第二階段：保存資料，讓 Lesson 進入 DB 並產生可被查詢的狀態
                    // ---------------------------------------------------------
                    System.out.println("💾 [2/3] 正在寫入資料庫...");
                    journeyRepository.saveAll(journeys);
                    journeyRepository.flush(); // 強制同步到資料庫

                    // ---------------------------------------------------------
                    // 第三階段：處理 Gym -> Lesson 的關聯 (relatedLessons)
                    // ---------------------------------------------------------
                    System.out.println("🔗 [3/3] 正在建立 Gym 與 Lesson 的關聯...");
                    boolean needUpdate = false;

                    for (Journey journey : journeys) {
                        if (journey.getChapters() != null) {
                            for (Chapter chapter : journey.getChapters()) {
                                if (chapter.getGyms() != null) {
                                    for (Gym gym : chapter.getGyms()) {

                                        // ★★★ 修改這裡 ★★★
                                        if (gym.getRelatedLessonIds() != null && !gym.getRelatedLessonIds().isEmpty()) {

                                            // 因為現在 getRelatedLessonIds() 已經是 List<String> 了，直接拿來用即可
                                            List<String> targetOriginalIds = gym.getRelatedLessonIds();

                                            // 去 DB 透過 original_id 找回真正的 Lesson Entity
                                            List<Lesson> lessons = lessonRepository.findByOriginalIdIn(targetOriginalIds);

                                            // 設定關聯
                                            gym.setRelatedLessons(lessons);
                                            needUpdate = true;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 如果有建立新的關聯，再存一次
                    if (needUpdate) {
                        journeyRepository.saveAll(journeys);
                    }

                    System.out.println("✅ Journey JSON 匯入成功！Original ID 與關聯皆已建立。");

                } catch (Exception e) {
                    System.err.println("❌ Journey 匯入失敗: " + e.getMessage());
                    e.printStackTrace();
                    throw e; // 拋出異常讓 Transaction Rollback
                }
            } else {
                System.out.println("ℹ️ Journey 資料已存在，跳過匯入。");
            }
        };
    }
}