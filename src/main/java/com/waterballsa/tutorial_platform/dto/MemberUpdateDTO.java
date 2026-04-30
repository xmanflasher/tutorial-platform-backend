package com.waterballsa.tutorial_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberUpdateDTO {
    @NotBlank(message = "暱稱不得為空")
    @Size(max = 20, message = "暱稱長度不得超過 20 字元")
    private String nickName;

    private String avatar;

    @Size(max = 50, message = "職稱長度不得超過 50 字元")
    private String jobTitle;

    private String region;

    @Size(max = 50, message = "職業長度不得超過 50 字元")
    private String occupation;
}
