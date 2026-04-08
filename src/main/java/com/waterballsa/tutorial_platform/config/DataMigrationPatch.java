package com.waterballsa.tutorial_platform.config;

import com.waterballsa.tutorial_platform.entity.Member;
import com.waterballsa.tutorial_platform.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * [Migration Patch] 2024-04-07
 * 針對 RBAC 改版後產生的舊資料進行 Role 字串補強。
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataMigrationPatch implements CommandLineRunner {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("🚀 啟動資料庫修補任務：檢查 Member Role 缺失情況...");
        
        // 1. 查找所有 Role 為空的舊資料
        List<Member> emptyRoleMembers = memberRepository.findAll().stream()
                .filter(m -> m.getRole() == null)
                .toList();

        if (!emptyRoleMembers.isEmpty()) {
            log.warn("⚠️ 發現 {} 位使用者 Role 缺失，正在進行即時補貼 (ROLE_USER)...", emptyRoleMembers.size());
            emptyRoleMembers.forEach(m -> {
                 m.setRole(Member.Role.ROLE_USER);
            });
            // 批次更新
            memberRepository.saveAll(emptyRoleMembers);
            log.info("✅ 已成功修復 {} 筆髒資料。", emptyRoleMembers.size());
        } else {
            log.info("✨ 未發現髒資料，資料庫狀態良好。");
        }
    }
}
