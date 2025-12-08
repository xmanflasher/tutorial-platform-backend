package com.waterballsa.tutorial_platform.service;

import com.waterballsa.tutorial_platform.dto.*;
import com.waterballsa.tutorial_platform.entity.*;
import com.waterballsa.tutorial_platform.repository.JourneyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator; // ★ 記得引入 Comparator
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JourneyService {

    private final JourneyRepository journeyRepository;

    public List<JourneyDetailDTO> getAllJourneys() {
        return journeyRepository.findAll().stream()
                .map(this::toDetailDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public JourneyDetailDTO getJourneyBySlug(String slug) {
        Journey journey = journeyRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Journey not found: " + slug));

        return toDetailDTO(journey);
    }

    // --- 核心轉換邏輯 (Entity -> DTO) ---
    private JourneyDetailDTO toDetailDTO(Journey entity) {

        // 1. 計算總影片數 (這裡計算所有存在的影片，包含隱藏的，如果是只要計算顯示的，要加 filter)
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

        // 3. 轉換 Chapters & Lessons (加入 排序 與 過濾)
        List<ChapterDTO> chapterDTOs = Collections.emptyList();
        if (entity.getChapters() != null) {
            chapterDTOs = entity.getChapters().stream()
                    // ★ 1. 過濾 Chapter：只留下 visible 為 true 的
                    .filter(ch -> Boolean.TRUE.equals(ch.getVisible()))

                    // ★ 2. 排序 Chapter：依照 displayOrder
                    .sorted(Comparator.comparing(Chapter::getDisplayOrder, Comparator.nullsLast(Comparator.naturalOrder())))

                    .map(chapter -> {
                        List<LessonDTO> lessonDTOs = Collections.emptyList();
                        if (chapter.getLessons() != null) {
                            lessonDTOs = chapter.getLessons().stream()
                                    // ★ 3. 過濾 Lesson：只留下 visible 為 true 的
                                    .filter(lesson -> Boolean.TRUE.equals(lesson.getVisible()))

                                    // ★ 4. 排序 Lesson：依照 displayOrder
                                    .sorted(Comparator.comparing(Lesson::getDisplayOrder, Comparator.nullsLast(Comparator.naturalOrder())))

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
            menuDTOs = entity.getMenus().stream().map(menu ->
                    JourneyMenuDTO.builder()
                            .name(menu.getName())
                            .href(menu.getHref())
                            .icon(menu.getIcon())
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