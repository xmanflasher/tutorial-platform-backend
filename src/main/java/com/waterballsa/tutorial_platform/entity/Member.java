package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "members")
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ToString.Include
    private String name;
    @ToString.Include
    private String email;
    private String avatar;
    private String jobTitle;
    private String nickName;
    private String occupation;

    public enum Role {
        ROLE_GUEST, ROLE_USER, ROLE_INSTRUCTOR, ROLE_ADMIN
    }

    @Column(nullable = false, columnDefinition = "varchar(20) default 'ROLE_USER'")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @ToString.Include
    private Role role = Role.ROLE_USER;

    public Role getRole() {
        return role == null ? Role.ROLE_USER : role;
    }

    // ★ 講師額外欄位 (Instructor Info)
    private String instructorBio;
    private String socialLinks;

    @Builder.Default
    private Long nextLevelExp = 2000L;

    @Builder.Default
    private Integer level = 1;

    @Builder.Default
    private Long exp = 0L;

    @Builder.Default
    private Long coin = 0L;

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("member")
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    @BatchSize(size = 100)
    @Builder.Default
    @ToString.Exclude
    private List<LearningRecord> learningRecords = new ArrayList<>();

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("instructor")
    @OneToMany(mappedBy = "instructor", cascade = CascadeType.ALL)
    @BatchSize(size = 100)
    @Builder.Default
    @ToString.Exclude
    private List<Journey> journeys = new ArrayList<>();

    private String sex;
    private String birthDate;
    private LocalDateTime subscriptionEndDate;

    private String region;
    private String githubUrl;
    private String discordId;

    private String originVisitorId;
    private String visitorCategory; // GUEST, PASSERBY

    private String password;

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 【領域行為】獲取獎勵並處理等級提升邏輯
     */
    public void earnReward(Reward reward) {
        if (reward == null) return;
        
        // 1. 金幣發放
        this.coin = (this.coin == null ? 0L : this.coin) + (reward.getCoin() != null ? reward.getCoin() : 0);

        // 2. 經驗發放與升級演算
        if (reward.getExp() != null) {
            this.exp = (this.exp == null ? 0L : this.exp) + reward.getExp().longValue();
            checkAndPerformLevelUp();
        }

        // 3. 訂閱期限延長
        if (reward.getSubscriptionExtensionInDays() != null) {
            extendSubscription(reward.getSubscriptionExtensionInDays());
        }
    }

    public void extendSubscription(int days) {
        if (days <= 0) return;
        LocalDateTime baseDate = (this.subscriptionEndDate == null || this.subscriptionEndDate.isBefore(LocalDateTime.now())) 
                                ? LocalDateTime.now() 
                                : this.subscriptionEndDate;
        this.subscriptionEndDate = baseDate.plusDays(days);
    }

    private void checkAndPerformLevelUp() {
        if (this.nextLevelExp == null) this.nextLevelExp = 2000L;
        if (this.level == null) this.level = 1;

        while (this.exp >= this.nextLevelExp) {
            this.exp -= this.nextLevelExp;
            this.level++;
            this.nextLevelExp = (long)(this.nextLevelExp * 1.2);
        }
    }
}