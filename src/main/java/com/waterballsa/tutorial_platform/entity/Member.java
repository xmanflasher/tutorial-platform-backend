package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@Data // 👈 這個註解會自動產生 getCoin(), getExp()
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
    private String nickName;   // 暱稱 (e.g. CW)
    private String occupation; // 職業代碼 (e.g. JUNIOR_PROGRAMMER)

    @Builder.Default
    private Long nextLevelExp = 2000L; // 下一級所需經驗值 (可設預設值)

    @Builder.Default
    private Integer level = 1;

    // ★★★ 關鍵修正：必須要有這兩個欄位 ★★★
    @Builder.Default
    private Long exp = 0L;   // 經驗值

    @Builder.Default
    private Long coin = 0L;  // 金幣

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    @Builder.Default
    private List<LearningRecord> learningRecords = new ArrayList<>();

    // 其他欄位 (如 sex, birthDate 等)
    private String sex;
    private String birthDate;
    private LocalDateTime subscriptionEndDate;
}