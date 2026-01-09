package com.waterballsa.tutorial_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeDto {
    private String id;
    private String name;
    private Boolean obtained;
}