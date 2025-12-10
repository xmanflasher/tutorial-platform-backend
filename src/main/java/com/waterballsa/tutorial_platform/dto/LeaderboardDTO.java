package com.waterballsa.tutorial_platform.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaderboardDTO {
    private Long id;
    private String name;
    private String avatar;
    private Long exp;
    private Integer level;
    private String jobTitle;
}