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

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final LessonRepository lessonRepository;
    // 注入 GymRepository 確保我們可以獨立儲存 Gym (有時候只存 Journey 關聯會沒更新到)
    private final GymRepository gymRepository;

    @Bean
    @Transactional
    CommandLineRunner initDatabase(JourneyRepository journeyRepository) {
        return args -> {
            if (journeyRepository.count() == 0) {
                System.out.println("🚀 [1/3] 開始匯入 Journey JSON ...");
                ObjectMapper mapper = new ObjectMapper();
                // 忽略 JSON 裡有但 Entity 裡沒有的欄位
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                try {
                    InputStream inputStream = new ClassPathResource("data.json").getInputStream();
                    List<Journey> journeys = mapper.readValue(inputStream, new TypeReference<List<Journey>>() {});

                    // ---------------------------------------------------------
                    // 第一階段：清理 ID 並建立層級關聯 (Parent-Child)
                    // ---------------------------------------------------------
                    journeys.forEach(journey -> {
                        journey.setOriginalId(String.valueOf(journey.getId()));
                        journey.setId(null);

                        // Skills
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
                                // 3-1. 清洗 Mission 本身的 ID
                                mission.setOriginalId(String.valueOf(mission.getId()));
                                mission.setId(null); // ★ 必做：清除 Mission ID
                                mission.setJourney(journey);

                                // 3-2. 處理 Prerequisites (前置條件)
                                // JSON 對應欄位: "prerequisites"
                                if (mission.getPrerequisites() != null) {
                                    mission.getPrerequisites().forEach(req -> {
                                        // 備份舊 ID (如果有的話)
                                        if (req.getId() != null) {
                                            req.setOriginalId(String.valueOf(req.getId()));
                                        }

                                        // ★★★ 關鍵修正：徹底清除 ID，讓 Hibernate 視為新資料 ★★★
                                        req.setId(null);

                                        // ★ 補上 DB 必填欄位 (Entity 中 nullable=false)
                                        req.setCategory("PREREQUISITE");

                                        // ★ 建立關聯
                                        req.setMission(mission);
                                    });
                                }

                                // 3-3. 處理 Criteria (驗收條件)
                                // JSON 對應欄位: "criteria"
                                if (mission.getCriteria() != null) {
                                    mission.getCriteria().forEach(req -> {
                                        if (req.getId() != null) {
                                            req.setOriginalId(String.valueOf(req.getId()));
                                        }

                                        // ★★★ 關鍵修正：徹底清除 ID ★★★
                                        req.setId(null);

                                        // ★ 補上 DB 必填欄位
                                        req.setCategory("CRITERIA");

                                        // ★ 建立關聯
                                        req.setMission(mission);
                                    });
                                }
                            });
                        }

                        // Menus (選單)
                        if (journey.getMenus() != null) {
                            journey.getMenus().forEach(menu -> {
                                menu.setId(null);
                                menu.setJourney(journey);
                            });
                        }

                        // Chapters
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

                                // Gyms
                                if (chapter.getGyms() != null) {
                                    chapter.getGyms().forEach(gym -> {
                                        gym.setOriginalId(String.valueOf(gym.getId()));
                                        gym.setId(null);
                                        gym.setChapter(chapter);
                                        // Challenges
                                        if (gym.getChallenges() != null) {
                                            gym.getChallenges().forEach(c -> {
                                                c.setOriginalId(String.valueOf(c.getId()));
                                                c.setId(null);
                                                c.setGym(gym);
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });

                    // ---------------------------------------------------------
                    // 第二階段：先存檔，讓 Lesson 產生 ID，這樣後續才能查
                    // ---------------------------------------------------------
                    System.out.println("💾 [2/3] 正在寫入資料庫 (第一次儲存)...");
                    journeyRepository.saveAll(journeys);
                    journeyRepository.flush(); // 強制寫入

                    // ---------------------------------------------------------
                    // 第三階段：建立 Gym <-> Lesson 多對多關聯 (修正 ID 格式問題)
                    // ---------------------------------------------------------
                    System.out.println("🔗 [3/3] 正在解析 relatedLessonIds 並建立關聯...");

                    for (Journey journey : journeys) {
                        if (journey.getChapters() != null) {
                            for (Chapter chapter : journey.getChapters()) {
                                if (chapter.getGyms() != null) {
                                    for (Gym gym : chapter.getGyms()) {

                                        List<String> rawIds = gym.getRelatedLessonIds();

                                        if (rawIds != null && !rawIds.isEmpty()) {

                                            // ★★★ 修正重點：清洗 ID 格式 ★★★
                                            // 將 "3_18" 這種格式轉換成 "18"
                                            List<String> cleanIds = rawIds.stream()
                                                    .map(id -> {
                                                        if (id.contains("_")) {
                                                            // 取底線後面那一段 (假設 ID 是唯一的)
                                                            return id.substring(id.lastIndexOf("_") + 1);
                                                        }
                                                        return id;
                                                    })
                                                    .toList(); // Java 16+ 寫法，如果是舊版可用 .collect(Collectors.toList())

                                            // 使用清洗後的 ID 去找 Lesson
                                            List<Lesson> lessons = lessonRepository.findByOriginalIdIn(cleanIds);

                                            if (!lessons.isEmpty()) {
                                                // (選用) 印出除錯資訊，確認是否有找到正確數量
                                                // System.out.println("   - Gym [" + gym.getName() + "] 原始ID: " + rawIds + " -> 找到: " + lessons.size() + " 堂課");

                                                gym.setRelatedLessons(lessons);
                                                gymRepository.save(gym);
                                            } else {
                                                // 如果清洗後還是找不到，印出更詳細的資訊方便除錯
                                                System.err.println("   ! 警告: Gym [" + gym.getName() + "] 找不到 Lesson。搜尋 ID: " + cleanIds);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    System.out.println("✅ 資料匯入完成！");

                } catch (Exception e) {
                    System.err.println("❌ 匯入失敗: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
            } else {
                System.out.println("ℹ️ 資料庫已有資料，跳過 Seeder。");
            }
        };
    }
}