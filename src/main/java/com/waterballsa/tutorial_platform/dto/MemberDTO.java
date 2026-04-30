package com.waterballsa.tutorial_platform.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberDTO {
    private Long id;           // ★ 補上 ID，前端路由需要
    private String name;
    @Size(max = 20, message = "暱稱長度不得超過 20 字元")
    private String nickName;   // ★ 新增
    @Size(max = 50, message = "職稱長度不得超過 50 字元")
    private String jobTitle;
    @Size(max = 50, message = "職業長度不得超過 50 字元")
    private String occupation; // ★ 新增
    private Integer level;
    private Long exp;
    private Long nextLevelExp; // ★ 新增

    private String avatar;     // 這是給 /api/me 用的
    private String pictureUrl; // ★ 新增：這是給 /api/users 用的 (前端 Portfolio 習慣用這個名字)

    private String email;      // 注意：公開 API 不該回傳這個
    private String sex;
    private String birthDate;

    // ★ 新增個人檔案欄位
    private String region;
    private String githubUrl;
    private String discordId;

    // ★ RBAC 與講師欄位
    private String role;
    private String instructorBio;
    private String socialLinks;
}