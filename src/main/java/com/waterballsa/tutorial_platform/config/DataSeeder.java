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
import java.time.LocalDateTime;
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
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                try {
                    InputStream inputStream = new ClassPathResource("data.json").getInputStream();
                    List<Journey> journeys = mapper.readValue(inputStream, new TypeReference<List<Journey>>() {});

                    // ▼▼▼▼▼ 最終修正邏輯 ▼▼▼▼▼
                    journeys.forEach(journey -> {
                        journey.setId(null); // 1. 清除 Journey ID

                        // ★ 修正 1：Skill 是跟著 Journey 的
                        if (journey.getSkills() != null) {
                            journey.getSkills().forEach(skill -> {
                                skill.setId(null);         // 清除 Skill ID
                                // skill.setJourney(journey); // 如果 Skill 有對應 Journey 的欄位，請取消註解這行
                            });
                        }
                        // 處理 Missions
                        if (journey.getMissions() != null) {
                            journey.getMissions().forEach(mission -> {
                                mission.setId(null);          // 清除 Mission ID
                                mission.setJourney(journey);  // 建立關聯

                                // 1. 處理 Reward (原本有的)
                                if (mission.getReward() != null) {
                                    mission.getReward().setId(null);
                                }

                                // 👇👇👇【新增】處理 Prerequisites (先修條件) 👇👇👇
                                if (mission.getPrerequisites() != null) {
                                    mission.getPrerequisites().forEach(prerequisite -> {
                                        prerequisite.setId(null); // ★ 清除 ID
                                        // 如果 Prerequisite 有 mission 欄位，記得建立關聯：
                                        // prerequisite.setMission(mission);
                                    });
                                }

                                // 👇👇👇【新增】處理 Criteria (驗收標準) 👇👇👇
                                if (mission.getCriteria() != null) {
                                    mission.getCriteria().forEach(criterion -> {
                                        criterion.setId(null); // ★ 清除 ID
                                        // 如果 Criterion 有 mission 欄位，記得建立關聯：
                                        // criterion.setMission(mission);
                                    });
                                }
                            });
                        }
                        if (journey.getChapters() != null) {
                            journey.getChapters().forEach(chapter -> {
                                chapter.setId(null);
                                chapter.setJourney(journey);

                                // 處理 Lessons
                                if (chapter.getLessons() != null) {
                                    chapter.getLessons().forEach(lesson -> {
                                        lesson.setId(null);
                                        lesson.setChapter(chapter);

                                        // ★ 修正 2：處理 Lesson 的 Reward
                                        if (lesson.getReward() != null) {
                                            lesson.getReward().setId(null);
                                        }

                                        // 再次確認：Skill 不在 Lesson 下，所以這裡不用處理 Skill
                                    });
                                }

                                // 處理 Gyms
                                if (chapter.getGyms() != null) {
                                    chapter.getGyms().forEach(gym -> {
                                        gym.setId(null);
                                        gym.setChapter(chapter);

                                        // ★ 修正 3：處理 Gym 的 Reward (這是確定的)
                                        if (gym.getReward() != null) {
                                            gym.getReward().setId(null);
                                        }

                                        // 處理 Challenges
                                        if (gym.getChallenges() != null) {
                                            gym.getChallenges().forEach(challenge -> {
                                                challenge.setId(null);
                                                // 如果 Challenge 也有 reward，這裡也要加
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
            }

            // ... (Part 2 & Part 3 保持不變) ...

            System.out.println("🎉 所有資料初始化完成！");
        };
    }
}