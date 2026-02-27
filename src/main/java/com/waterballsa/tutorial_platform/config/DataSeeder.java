package com.waterballsa.tutorial_platform.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waterballsa.tutorial_platform.entity.*;
import com.waterballsa.tutorial_platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final JourneyRepository journeyRepository;
    private final ChapterRepository chapterRepository;
    private final LessonRepository lessonRepository;
    private final GymRepository gymRepository;
    private final MissionRepository missionRepository;
    private final RequirementRepository requirementRepository;
    private final MemberRepository memberRepository;
    private final GymSubmissionRepository gymSubmissionRepository;
    private final MemberMissionRepository memberMissionRepository;

    private final IdMapper idMapper = new IdMapper();
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (journeyRepository.count() > 0) {
            System.out.println("ℹ️ 資料庫已有資料，跳過 Seeder。");
            return;
        }

        System.out.println("🚀 開始 DataSeeder...");

        // 1. 匯入實體
        importEntities();

        // 2. 建立關聯
        linkRelationships();

        // 3. 修正 MissionRequirements 的 targetIds (新創帳號開啟條件)
        resolveMissionRequirements();

        // 4. 建立通關帳號
        seedMasterAccount();

        System.out.println("🎉 全部完成！");
    }

    private void seedMasterAccount() {
        System.out.println("Processing Pass 4: Seeding Master Account...");
        String email = "master@test.com";

        if (memberRepository.findByEmail(email).isPresent()) {
            System.out.println("   ℹ️ Master account already exists.");
            return;
        }

        Member master = Member.builder()
                .name("傳說中的通關者")
                .email(email)
                .password("pass123")
                .occupation("架構師")
                .avatar("https://api.dicebear.com/7.x/miniavs/svg?seed=master")
                .nickName("再一次就掛機")
                .region("台北市")
                .githubUrl("https://github.com/Waterballsa")
                .discordId("Legendary#9999")
                .level(99)
                .exp(999999L)
                .coin(999999L)
                .subscriptionEndDate(LocalDateTime.now().plusYears(100))
                .build();

        master = memberRepository.save(master);
        final Member finalMaster = master;

        // 1. 通關所有道館
        gymRepository.findAll().forEach(gym -> {
            gymSubmissionRepository.save(GymSubmission.builder()
                    .member(finalMaster)
                    .gym(gym)
                    .status(GymSubmission.SubmissionStatus.SUCCESS)
                    .submittedAt(LocalDateTime.now())
                    .build());
        });

        // 2. 完成所有任務並領取獎勵
        missionRepository.findAll().forEach(mission -> {
            memberMissionRepository.save(MemberMission.builder()
                    .member(finalMaster)
                    .mission(mission)
                    .status(MemberMission.MissionStatus.CLAIMED)
                    .currentProgress(100)
                    .completedAt(LocalDateTime.now())
                    .build());
        });

        System.out.println("   ✅ Master account created successfully.");
    }

    private void resolveMissionRequirements() {
        System.out.println("Processing Pass 3: Resolving MissionRequirement Target IDs...");
        List<MissionRequirement> allReqs = requirementRepository.findAll();
        
        for (MissionRequirement req : allReqs) {
            String type = req.getConditionType();
            Map<String, Object> params = req.getParams();
            if (params == null) continue;

            try {
                if ("MISSION_COMPLETED".equals(type)) {
                    Long jOid = ((Number) params.get("journeyId")).longValue();
                    Long mOid = ((Number) params.get("missionId")).longValue();
                    Long targetMid = idMapper.getMission(jOid, mOid);
                    if (targetMid != null) {
                        req.setTargetMissionId(targetMid);
                        requirementRepository.save(req);
                    }
                } else if (type.startsWith("GYM_CHALLENGE")) {
                    Long jOid = ((Number) params.get("journeyId")).longValue();
                    Long cOid = ((Number) params.get("chapterId")).longValue();
                    Long gOid = ((Number) params.get("gymId")).longValue();
                    Long targetGid = idMapper.getGym(jOid, cOid, gOid);
                    if (targetGid != null) {
                        req.setTargetGymId(targetGid);
                        requirementRepository.save(req);
                    }
                }
            } catch (Exception e) {
                System.err.println("   ❌ 無法解析 Requirement ID: " + req.getId() + " - " + e.getMessage());
            }
        }
        System.out.println("   ✅ Requirement Target IDs 解析完畢");
    }

    private void importEntities() throws Exception {
        System.out.println("Processing Pass 1: Saving Entities...");

        try (InputStream inputStream = new ClassPathResource("data.json").getInputStream()) {
            List<Journey> journeys = mapper.readValue(inputStream, new TypeReference<List<Journey>>() {});

            for (Journey journey : journeys) {
                // 備份子物件並切斷關聯
                List<Mission> missions = journey.getMissions();
                List<Chapter> chapters = journey.getChapters();
                List<Skill> skills = journey.getSkills();
                List<JourneyMenu> menus = journey.getMenus();

                journey.setMissions(null);
                journey.setChapters(null);
                journey.setSkills(null);
                journey.setMenus(null);

                // 儲存 Journey
                journey.setOriginalId(journey.getId());
                journey.setId(null);
                Journey savedJourney = journeyRepository.save(journey);
                idMapper.putJourney(journey.getOriginalId(), savedJourney.getId());

                // Missions
                if (missions != null) {
                    for (Mission m : missions) {
                        List<MissionRequirement> prereqs = m.getPrerequisites();
                        List<MissionRequirement> criteria = m.getCriteria();
                        m.setPrerequisites(null);
                        m.setCriteria(null);

                        m.setOriginalId(m.getId());
                        m.setId(null);
                        m.setJourney(savedJourney);
                        Mission savedMission = missionRepository.save(m);
                        idMapper.putMission(journey.getOriginalId(), m.getOriginalId(), savedMission.getId());

                        if (prereqs != null) saveRequirements(savedMission, prereqs, "PREREQUISITE");
                        if (criteria != null) saveRequirements(savedMission, criteria, "CRITERIA");
                    }
                }

                // Chapters
                if (chapters != null) {
                    for (Chapter c : chapters) {
                        List<Lesson> lessons = c.getLessons();
                        List<Gym> gyms = c.getGyms();
                        c.setLessons(null);
                        c.setGyms(null);

                        c.setOriginalId(c.getId());
                        c.setId(null);
                        c.setJourney(savedJourney);
                        Chapter savedChapter = chapterRepository.save(c);
                        idMapper.putChapter(journey.getOriginalId(), c.getOriginalId(), savedChapter.getId());

                        // Lessons
                        if (lessons != null) {
                            for (Lesson l : lessons) {
                                l.setOriginalId(l.getId());
                                l.setId(null);
                                l.setChapter(savedChapter);
                                l.setJourney(savedJourney);
                                lessonRepository.save(l);
                                idMapper.putLesson(journey.getOriginalId(), c.getOriginalId(), l.getOriginalId(), l.getId());
                            }
                        }

                        // Gyms
                        if (gyms != null) {
                            for (Gym g : gyms) {
                                if (g.getChallenges() != null) {
                                    g.getChallenges().forEach(ch -> {
                                        ch.setOriginalId(ch.getId());
                                        ch.setId(null);
                                        ch.setGym(g);
                                    });
                                }
                                g.setOriginalId(g.getId());
                                g.setId(null);
                                g.setChapter(savedChapter);
                                g.setJourney(savedJourney);
                                gymRepository.save(g);
                                idMapper.putGym(journey.getOriginalId(), c.getOriginalId(), g.getOriginalId(), g.getId());
                            }
                        }
                    }
                }

                if (menus != null) {
                    menus.forEach(m -> { m.setId(null); m.setJourney(savedJourney); });
                }
                if (skills != null) {
                    skills.forEach(s -> { s.setOriginalId(s.getId()); s.setId(null); s.setJourney(savedJourney); });
                }
            }
            journeyRepository.flush();
        }
    }

    private void linkRelationships() throws Exception {
        System.out.println("Processing Pass 2: Linking Gym-Lesson Relationships...");

        try (InputStream inputStream = new ClassPathResource("data.json").getInputStream()) {
            List<Journey> journeys = mapper.readValue(inputStream, new TypeReference<List<Journey>>() {});

            for (Journey journeyJson : journeys) {
                // ★★★ 修正點 1：讀取新 JSON 時，ID 是存在 getId() 裡 (因為 JSON key 是 "id")
                Long jOid = journeyJson.getId();

                if (journeyJson.getChapters() != null) {
                    for (Chapter chapterJson : journeyJson.getChapters()) {
                        Long cOid = chapterJson.getId(); // ★★★ 修正點 2

                        if (chapterJson.getGyms() != null) {
                            for (Gym gymJson : chapterJson.getGyms()) {
                                Long gOid = gymJson.getId(); // ★★★ 修正點 3

                                List<String> links = gymJson.getRelatedLessonIds();

                                if (links != null && !links.isEmpty()) {
                                    System.out.println("🔎 Gym " + gOid + " 需連結 " + links.size() + " 堂課");

                                    // 透過正確的 Original ID (gOid) 去 Mapper 查 DB ID
                                    Long gymDbId = idMapper.getGym(jOid, cOid, gOid);

                                    if (gymDbId != null) {
                                        Gym gymRef = gymRepository.getReferenceById(gymDbId);

                                        for (String link : links) {
                                            try {
                                                String[] parts = link.split("_");
                                                Long targetCOid = Long.parseLong(parts[0]);
                                                Long targetLOid = Long.parseLong(parts[1]);

                                                Long lessonDbId = idMapper.getLesson(jOid, targetCOid, targetLOid);

                                                if (lessonDbId != null) {
                                                    Lesson lessonInDb = lessonRepository.findById(lessonDbId).orElse(null);
                                                    if (lessonInDb != null) {
                                                        lessonInDb.setGym(gymRef);
                                                        lessonRepository.save(lessonInDb);
                                                        System.out.println("   ✅ 成功連結: Gym(" + gOid + ") -> Lesson(" + targetLOid + ")");
                                                    }
                                                } else {
                                                    System.err.println("   ❌ 找不到 Lesson DB ID: " + targetLOid);
                                                }
                                            } catch (Exception e) {
                                                System.err.println("   ❌ 解析失敗: " + link);
                                            }
                                        }
                                    } else {
                                        System.err.println("   ❌ 找不到 Gym DB ID (Mapper 查無資料): " + gOid);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void saveRequirements(Mission parent, List<MissionRequirement> list, String category) {
        if (list == null) return;
        for (MissionRequirement r : list) {
            r.setOriginalId(r.getId());
            r.setId(null);
            r.setMission(parent);
            r.setCategory(category);
            requirementRepository.save(r);
        }
    }
}
