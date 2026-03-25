package com.waterballsa.tutorial_platform.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LessonDTO {
    private String id;
    private String name;
    private String description;
    private String type;
    private String videoLength;
    private Boolean premiumOnly;
    private Boolean passwordRequired;
    private RewardDTO reward;

    // ★★★ 補上這兩個 ID 欄位 ★★★
    private String chapterId;
    private String journeyId;

    // ★★★ 加入 lesson content 以支援前端影片及文章播放 ★★★
    private java.util.List<java.util.Map<String, Object>> content;
}