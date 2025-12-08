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
public class JourneyDetailDTO {
    private String id;
    private String slug;
    private String title;
    private String description;
    private Integer totalVideos;
    private List<String> skills;
    private List<ChapterDTO> chapters;
    private List<JourneyMenuDTO> menus;

    // 前端需要的額外欄位
    private String subtitle;
    private Integer price;
    private ActionButtons actionButtons;

    // 這裡定義靜態內部類別，必須加上 @Data 和 @Builder
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionButtons {
        private String primary;
        private String secondary;
    }
}