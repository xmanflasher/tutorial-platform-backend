package com.waterballsa.tutorial_platform.service;

import com.waterballsa.tutorial_platform.dto.*;
import com.waterballsa.tutorial_platform.entity.*;
import com.waterballsa.tutorial_platform.repository.JourneyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JourneyService {

    private final JourneyRepository journeyRepository;

    public List<JourneyDetailDTO> getAllJourneys() {
        return journeyRepository.findAll().stream()
                // ★ 1. [新增] 過濾 Journey：只回傳 visible 為 true 的
                .filter(journey -> Boolean.TRUE.equals(journey.getVisible()))
                .map(this::toDetailDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public JourneyDetailDTO getJourneyBySlug(String slug) {
        Journey journey = journeyRepository.findBySlug(slug)
                // ★ 1. [新增] 過濾 Journey：即使是用 slug 查，若 invisible 也視為找不到
                .filter(j -> Boolean.TRUE.equals(j.getVisible()))
                .orElseThrow(() -> new RuntimeException("Journey not found: " + slug));

        return toDetailDTO(journey);
    }

    // --- 核心轉換邏輯 (Entity -> DTO) ---
    private JourneyDetailDTO toDetailDTO(Journey entity) {

        // 1. 計算總影片數
        int totalVideos = 0;
        if (entity.getChapters() != null) {
            totalVideos = entity.getChapters().stream()
                    .mapToInt(ch -> ch.getLessons() != null ? ch.getLessons().size() : 0)
                    .sum();
        }

        // 2. 轉換 Skills
        List<String> skills = Collections.emptyList();
        if (entity.getSkills() != null) {
            skills = entity.getSkills().stream()
                    .map(Skill::getName)
                    .collect(Collectors.toList());
        }

        // 3. 轉換 Chapters & Lessons
        List<ChapterDTO> chapterDTOs = Collections.emptyList();
        if (entity.getChapters() != null) {
            chapterDTOs = entity.getChapters().stream()
                    // 過濾 Chapter：只留下 visible 為 true 的
                    .filter(ch -> Boolean.TRUE.equals(ch.getVisible()))

                    // 排序 Chapter：依照 displayOrder (若您希望 Chapter 也依 ID 排，可在此修改)
                    .sorted(Comparator.comparing(Chapter::getDisplayOrder, Comparator.nullsLast(Comparator.naturalOrder())))

                    .map(chapter -> {
                        List<LessonDTO> lessonDTOs = Collections.emptyList();
                        if (chapter.getLessons() != null) {
                            lessonDTOs = chapter.getLessons().stream()
                                    // ★ 2. 過濾 Lesson：只留下 visible 為 true 的
                                    .filter(lesson -> Boolean.TRUE.equals(lesson.getVisible()))

                                    // ★ 3. [修改] 排序 Lesson：依照 ID 排序
                                    // 註：因為這裡已經在特定 Chapter 的迴圈內，所有 Lesson 的 chapter_id 都相同，
                                    // 所以直接對 ID 排序即可達成「先 Chapter 後 ID」的效果。
                                    .sorted(Comparator.comparing(Lesson::getId))

                                    .map(lesson -> {
                                        RewardDTO lessonRewardDTO = null;
                                        if (lesson.getReward() != null) {
                                            lessonRewardDTO = RewardDTO.builder()
                                                    .coin(lesson.getReward().getCoin())
                                                    .exp(lesson.getReward().getExp())
                                                    .description(lesson.getReward().getExternalRewardDescription())
                                                    .build();
                                        }

                                        return LessonDTO.builder()
                                                .id(String.valueOf(lesson.getId()))
                                                .chapterId(String.valueOf(chapter.getId()))
                                                .journeyId(String.valueOf(entity.getId()))
                                                .name(lesson.getName())
                                                .description(lesson.getDescription())
                                                .type(lesson.getType())
                                                .videoLength(lesson.getVideoLength())
                                                .passwordRequired(lesson.getPasswordRequired())
                                                .premiumOnly(lesson.getPremiumOnly())
                                                .reward(lessonRewardDTO)
                                                .build();
                                    }).collect(Collectors.toList());
                        }

                        return ChapterDTO.builder()
                                .id(String.valueOf(chapter.getId()))
                                .name(chapter.getName())
                                .lessons(lessonDTOs)
                                .build();
                    }).collect(Collectors.toList());
        }

        // 4. 轉換 Menus
        List<JourneyMenuDTO> menuDTOs = Collections.emptyList();
        if (entity.getMenus() != null) {
            menuDTOs = entity.getMenus().stream()
                    .filter(menu -> Boolean.TRUE.equals(menu.getVisible())) // 只撈 visible 的
                    .map(menu -> JourneyMenuDTO.builder()
                            .name(menu.getName())
                            .href(menu.getHref())
                            .icon(menu.getIcon()) // 確保這裡傳回字串 (e.g., "gift", "map")
                            .build()
                    ).collect(Collectors.toList());
        }

        // 5. 組裝最終 JourneyDetailDTO
        return JourneyDetailDTO.builder()
                .id(String.valueOf(entity.getId()))
                .slug(entity.getSlug())
                .title(entity.getName())
                .subtitle("成為硬核的 Coding 實戰高手")
                .description(entity.getDescription())
                .price(3000)
                .totalVideos(totalVideos)
                .skills(skills)
                .chapters(chapterDTOs)
                .actionButtons(JourneyDetailDTO.ActionButtons.builder()
                        .primary("立即加入課程")
                        .secondary("預約 1v1 諮詢")
                        .build())
                .menus(menuDTOs)
                .build();
    }
}