package com.waterballsa.tutorial_platform.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waterballsa.tutorial_platform.entity.*;
import com.waterballsa.tutorial_platform.repository.JourneyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(JourneyRepository repository) {
        return args -> {
            if (repository.count() > 0) return;

            System.out.println("🚀 開始匯入 data.json ...");
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            try {
                InputStream inputStream = new ClassPathResource("data.json").getInputStream();
                List<Journey> journeys = mapper.readValue(inputStream, new TypeReference<List<Journey>>() {});

                // ★ 定義黑名單：哪些章節或單元預設要隱藏 (不顯示在前端)
                List<String> hiddenChapterNames = List.of("課程介紹＆試聽", "規格驅動開發的前提");
                List<String> hiddenLessonNames = List.of("未公開的測試單元", "隱藏彩蛋"); // 舉例，你可以隨時加

                for (Journey journey : journeys) {
                    journey.setId(null);

                    // 處理 Skills
                    if (journey.getSkills() != null) {
                        journey.getSkills().forEach(s -> {
                            s.setId(null);
                            s.setJourney(journey);
                        });
                    }

                    // ★★★ 處理 Chapters & Lessons (包含排序與過濾) ★★★
                    if (journey.getChapters() != null) {
                        // 使用 for 迴圈以取得索引 i (作為排序依據)
                        for (int i = 0; i < journey.getChapters().size(); i++) {
                            Chapter chapter = journey.getChapters().get(i);
                            chapter.setId(null);
                            chapter.setJourney(journey);

                            // 1. 設定排序 (依照 JSON 陣列順序)
                            chapter.setDisplayOrder(i + 1); // 從 1 開始比較直觀

                            // 2. 設定是否顯示 (如果在黑名單中，則 visible = false)
                            boolean isChapterHidden = hiddenChapterNames.contains(chapter.getName());
                            chapter.setVisible(!isChapterHidden);

                            if (chapter.getLessons() != null) {
                                for (int j = 0; j < chapter.getLessons().size(); j++) {
                                    Lesson lesson = chapter.getLessons().get(j);
                                    lesson.setId(null);
                                    lesson.setChapter(chapter);

                                    // 3. 設定 Lesson 排序
                                    lesson.setDisplayOrder(j + 1);

                                    // 4. 設定 Lesson 是否顯示
                                    boolean isLessonHidden = hiddenLessonNames.contains(lesson.getName());
                                    lesson.setVisible(!isLessonHidden);

                                    // 處理 Reward
                                    if (lesson.getReward() != null) {
                                        lesson.getReward().setDbId(null);
                                    }
                                }
                            }
                        }
                    }

                    // ... (Missions 處理邏輯保持不變) ...
                    if (journey.getMissions() != null) {
                        // ... 你的 Missions 程式碼 ...
                        for (Mission mission : journey.getMissions()) {
                            mission.setJourney(journey);
                            mission.setId(null);
                            if (mission.getReward() != null) mission.getReward().setDbId(null);
                            if (mission.getPrerequisites() != null) {
                                mission.getPrerequisites().forEach(p -> { p.setMission(mission); p.setId(null); });
                            }
                            if (mission.getCriteria() != null) {
                                mission.getCriteria().forEach(c -> { c.setMission(mission); c.setId(null); });
                            }
                        }
                    }

                    // ... (Menu 處理邏輯保持不變) ...
                    String slug = journey.getSlug();
                    if ("software-design-pattern".equals(slug)) {
                        // ...
                        List<JourneyMenu> menus = new ArrayList<>();
                        // ... 你的 Menu 程式碼 ...
                        // 記得如果要重跑 Seeder，建議把 JourneyMenu 也改成 Lombok @Builder 寫法會更乾淨
                        // 這裡為了節省篇幅省略重複程式碼

                        // 範例：如果 Menu 已經改用 Lombok
                        menus.add(JourneyMenu.builder().name("所有單元").href("/journeys/software-design-pattern").icon("layers").displayOrder(1).journey(journey).build());
                        menus.add(JourneyMenu.builder().name("挑戰地圖").href("/challenges").icon("map").displayOrder(2).journey(journey).build());
                        menus.add(JourneyMenu.builder().name("SOP 寶典").href("/sop").icon("book-open").displayOrder(3).journey(journey).build());
                        journey.setMenus(menus);

                    } else if ("ai-bdd".equals(slug)) {
                        List<JourneyMenu> menus = new ArrayList<>();
                        menus.add(JourneyMenu.builder().name("所有單元").href("/journeys/ai-bdd").icon("layers").displayOrder(1).journey(journey).build());
                        menus.add(JourneyMenu.builder().name("Prompt 寶典").href("/journeys/ai-bdd/prompts").icon("sparkles").displayOrder(2).journey(journey).build());
                        journey.setMenus(menus);
                    }
                }

                repository.saveAll(journeys);
                System.out.println("🎉 匯入完成！");

            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }
}