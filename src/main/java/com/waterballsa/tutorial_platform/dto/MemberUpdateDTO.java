package com.waterballsa.tutorial_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberUpdateDTO {
    private String nickName;
    private String avatar;
    private String jobTitle;
    private String region;
    private String occupation;
}
