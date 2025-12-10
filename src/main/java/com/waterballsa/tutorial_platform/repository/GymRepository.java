package com.waterballsa.tutorial_platform.repository;

import com.waterballsa.tutorial_platform.entity.Gym;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GymRepository extends JpaRepository<Gym, Long> {

    // 挑戰地圖：撈出所有道館，並依照顯示順序 (displayOrder) 排列
    List<Gym> findAllByOrderByDisplayOrderAsc();
}