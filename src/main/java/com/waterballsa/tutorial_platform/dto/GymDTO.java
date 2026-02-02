package com.waterballsa.tutorial_platform.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GymDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Long chapterId; // ★ 前端分組需要這個
    private Integer maxStars;
    private String type;        // "CHALLENGE" 或 "BOSS"，用來決定圖示大小
    private Integer difficulty; // 難度
    private Integer rewardExp;  // 獎勵經驗值
}