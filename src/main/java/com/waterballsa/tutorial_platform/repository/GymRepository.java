package com.waterballsa.tutorial_platform.repository;

import com.waterballsa.tutorial_platform.entity.Gym;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GymRepository extends JpaRepository<Gym, Long> {

    // 這是之前解決編譯錯誤加的
    List<Gym> findAllByOrderByDisplayOrderAsc();

    // ★★★ 新增這個：給 Controller 用來解決 404 黑屏 ★★★
    // 意思：SELECT * FROM gyms WHERE original_id = ?
    Optional<Gym> findByOriginalId(Long originalId);
}