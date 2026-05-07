package com.waterballsa.tutorial_platform.service;

import com.waterballsa.tutorial_platform.dto.SkillStatDTO;
import com.waterballsa.tutorial_platform.entity.Challenge;
import com.waterballsa.tutorial_platform.entity.GymChallengeRecord;
import com.waterballsa.tutorial_platform.entity.Member;
import com.waterballsa.tutorial_platform.entity.SkillRating;
import com.waterballsa.tutorial_platform.entity.SkillRatingId;
import com.waterballsa.tutorial_platform.enums.ChallengeType;
import com.waterballsa.tutorial_platform.repository.ChallengeRepository;
import com.waterballsa.tutorial_platform.repository.GymChallengeRecordRepository;
import com.waterballsa.tutorial_platform.repository.SkillRatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillRatingService {
    private final SkillRatingRepository repository;
    private final GymChallengeRecordRepository recordRepository;
    private final ChallengeRepository challengeRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final Map<String, Double> GRADE_TO_SCORE = Map.of(
            "SS", 100.0,
            "S", 95.0,
            "A", 85.0,
            "B", 75.0,
            "C", 65.0,
            "D", 55.0,
            "F", 0.0
    );

    private static final List<String> RATING_ORDER = Arrays.asList(
            "SSS", "SS+", "SS", "S+", "S", "A+", "A", "B+", "B", "C+", "C", "D+", "D", "E+", "E", "F+", "F", "F-"
    );

    private static final Map<String, String> SKILL_LABELS = new LinkedHashMap<>() {{
        put("Logic", "邏輯思維");
        put("Arch", "架構設計");
        put("Design", "程式設計");
        put("Comm", "溝通協作");
        put("Solv", "問題解決");
    }};

    @Transactional
    public void updateSkillRating(Member member, Map<String, String> ratings, Long journeyId) {
        if (ratings == null || ratings.isEmpty()) return;
        
        log.info("[SkillRating] Updating stats for member {} (Journey: {}): {}", member.getId(), journeyId, ratings);

        // [ISSUE-29-01] 雙重寫入：更新專屬課程評分與全站綜合評分 (journeyId=0)
        saveAndIncrement(member, ratings, journeyId);
        saveAndIncrement(member, ratings, 0L);
    }

    private void saveAndIncrement(Member member, Map<String, String> ratings, Long journeyId) {
        SkillRatingId id = SkillRatingId.builder()
                .memberId(member.getId())
                .journeyId(journeyId)
                .build();

        SkillRating skillRating = repository.findById(id)
                .orElse(SkillRating.builder().id(id).member(member).build());

        if (skillRating.getScores() == null) {
            skillRating.setScores(new java.util.HashMap<>());
        }

        // [ISSUE-27-02] 映射到通用維度
        Map<String, Double> newScores = com.waterballsa.tutorial_platform.util.SkillMapper.mapToGeneralSkills(ratings, GRADE_TO_SCORE);

        for (Map.Entry<String, Double> entry : newScores.entrySet()) {
            String dimension = entry.getKey();
            Double score = entry.getValue();

            // [ISSUE-27-01] EMA 增量計算
            Double currentEma = skillRating.getScores().getOrDefault(dimension, score);
            double newEma = (currentEma * 0.5) + (score * 0.5);
            skillRating.getScores().put(dimension, newEma);
        }

        repository.saveAndFlush(skillRating);
    }

    public SkillRating getSkillRating(Long memberId, Long journeyId) {
        SkillRatingId id = SkillRatingId.builder()
                .memberId(memberId)
                .journeyId(journeyId)
                .build();
        return repository.findById(id).orElse(null);
    }

    /**
     * [ARCH-FIX-02] 課程專屬技能統計 (後端 SSOT 計算)
     */
    public List<SkillStatDTO> getJourneySkillStats(Long userId, Long journeyId) {
        // 1. 撈取該使用者的所有紀錄
        List<GymChallengeRecord> records = recordRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        // 2. 初始化最大分值
        Map<String, String> maxRatings = new HashMap<>();
        SKILL_LABELS.keySet().forEach(key -> maxRatings.put(key, "-"));

        // 3. 過濾並計算
        for (GymChallengeRecord record : records) {
            // 過濾課程
            if (journeyId != null && !journeyId.equals(record.getJourneyId())) continue;
            // 過濾狀態 (必須 SUCCESS 且不為空)
            if (record.getStatus() != GymChallengeRecord.ChallengeStatus.SUCCESS) continue;

            // 過濾類型 (僅限實作挑戰)
            Challenge challenge = challengeRepository.findById(record.getGymChallengeId()).orElse(null);
            if (challenge == null || challenge.getType() != ChallengeType.PRACTICAL_CHALLENGE) continue;

            if (record.getRatings() != null) {
                record.getRatings().forEach((dim, rating) -> {
                    String generalDim = mapDimension(dim);
                    if (SKILL_LABELS.containsKey(generalDim)) {
                        String currentRating = rating;
                        String bestSoFar = maxRatings.get(generalDim);

                        if ("-".equals(bestSoFar)) {
                            maxRatings.put(generalDim, currentRating);
                        } else {
                            int currentIndex = RATING_ORDER.indexOf(currentRating);
                            int bestIndex = RATING_ORDER.indexOf(bestSoFar);
                            // RATING_ORDER 越前面分越高
                            if (currentIndex != -1 && (bestIndex == -1 || currentIndex < bestIndex)) {
                                maxRatings.put(generalDim, currentRating);
                            }
                        }
                    }
                });
            }
        }

        // 4. 轉換為 DTO 列表 (維持 SKILL_LABELS 定義的順序)
        return SKILL_LABELS.entrySet().stream()
                .map(entry -> new SkillStatDTO(entry.getValue(), maxRatings.get(entry.getKey())))
                .collect(Collectors.toList());
    }

    private String mapDimension(String dim) {
        if (SKILL_LABELS.containsKey(dim)) return dim;
        return switch (dim) {
            case "1" -> "Logic";
            case "2", "3", "4" -> "Arch";
            case "5" -> "Design";
            case "6" -> "Solv";
            default -> dim;
        };
    }
}
