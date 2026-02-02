package com.waterballsa.tutorial_platform.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class GymDetailDTO {
    private Long id;
    private String code;
    private String name;
    private String description;


    // 挑戰列表
    private List<ChallengeDTO> challenges;

    // 關聯的課程列表 (讓前端可以直接渲染列表)
    private List<LessonDTO> lessons;

    // 獎勵經驗值
    private Integer rewardExp;
}