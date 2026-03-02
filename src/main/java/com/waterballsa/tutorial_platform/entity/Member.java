package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String avatar;
    private String jobTitle;
    private String nickName;
    private String occupation;

    @Builder.Default
    private Long nextLevelExp = 2000L;

    @Builder.Default
    private Integer level = 1;

    @Builder.Default
    private Long exp = 0L;

    @Builder.Default
    private Long coin = 0L;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    @Builder.Default
    private List<LearningRecord> learningRecords = new ArrayList<>();

    private String sex;
    private String birthDate;
    private LocalDateTime subscriptionEndDate;

    // ★ 新增個人檔案欄位
    private String region;
    private String githubUrl;
    private String discordId;

    // ★ 訪客追蹤相關
    private String originVisitorId;
    private String visitorCategory; // GUEST, PASSERBY

    private String password;

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}