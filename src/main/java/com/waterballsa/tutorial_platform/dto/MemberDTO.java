package com.waterballsa.tutorial_platform.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberDTO {
    private String name;
    private String jobTitle;
    private Integer level;
    private Long exp;
    private String avatar;
    // 如果需要個人檔案的詳細資料，可以加在這裡，或另外建 ProfileDTO
    private String email;
    private String sex;
    private String birthDate;
}