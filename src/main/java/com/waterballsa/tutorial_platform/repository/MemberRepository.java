package com.waterballsa.tutorial_platform.repository;

import com.waterballsa.tutorial_platform.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 登入用：根據 Email 找使用者
    Optional<Member> findByEmail(String email);

    // 排行榜用：依照經驗值 (exp) 降序排列，取前 100 名
    List<Member> findTop100ByOrderByExpDesc();
}