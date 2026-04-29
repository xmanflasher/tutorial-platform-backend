package com.waterballsa.tutorial_platform.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InstructorProfileUpdateDTO {
    @Size(max = 1000, message = "簡介長度不得超過 1000 字元")
    private String instructorBio;
    private String socialLinks;
    @Size(max = 50, message = "職稱長度不得超過 50 字元")
    private String jobTitle;
}
