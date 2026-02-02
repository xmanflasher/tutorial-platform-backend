package com.waterballsa.tutorial_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JourneyProgressDTO {
    private String id;
    private String slug;
    private String title;
    private String description;

    // 這裡使用剛剛定義好的 JourneyMenuDTO
    private List<JourneyMenuDTO> menus;

    private Integer level;
    private Integer currentExp;
    private Integer maxExp;

    // ★★★ 必須補上這兩個，RoadmapView 才能畫圖 ★★★
    private List<ChapterDTO> chapters;
    private List<GymDTO> gyms;

    // 引用 MemberMissionDTO (請確保 MemberMissionDTO 也是 Lombok 格式)
    private List<MemberMissionDTO> missions;

    // 引用 BadgeDto
    private List<BadgeDto> badges;
}