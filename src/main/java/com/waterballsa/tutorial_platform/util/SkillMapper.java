package com.waterballsa.tutorial_platform.util;

import java.util.HashMap;
import java.util.Map;

/**
 * [ISSUE-27-02] 技能維度映射器
 * 將特定課程的評分指標映射到全站通用的 5 大維度：
 * 邏輯思維 (Logic)、程式設計 (Design)、架構設計 (Arch)、溝通協作 (Comm)、問題解決 (Solv)
 */
public class SkillMapper {

    // 映射表：將道館特例的 "1"~"6" 映射到通用維度
    private static final Map<String, String> DIMENSION_MAPPING = Map.of(
        "1", "Logic",   // 需求結構化分析 -> 邏輯思維
        "2", "Arch",    // 區分結構與行為 -> 架構設計
        "3", "Arch",    // 抽象/萃取能力 -> 架構設計
        "4", "Arch",    // 建立 Well-Defined Context -> 架構設計
        "5", "Design",  // 熟悉設計模式的 Form -> 程式設計
        "6", "Solv"     // 游刃有餘的開發能力 -> 問題解決
    );

    /**
     * 將原始評分資料轉換為通用維度的分數加總
     * 因為多個特化維度可能映射到同一個通用維度，這裡採取平均值或累計後的處理
     */
    public static Map<String, Double> mapToGeneralSkills(Map<String, String> rawRatings, Map<String, Double> gradeToScore) {
        Map<String, Double> mappedScores = new HashMap<>();
        Map<String, Integer> counts = new HashMap<>();

        for (Map.Entry<String, String> entry : rawRatings.entrySet()) {
            String generalDim = DIMENSION_MAPPING.getOrDefault(entry.getKey(), entry.getKey());
            Double score = gradeToScore.getOrDefault(entry.getValue().toUpperCase(), 0.0);

            mappedScores.put(generalDim, mappedScores.getOrDefault(generalDim, 0.0) + score);
            counts.put(generalDim, counts.getOrDefault(generalDim, 0) + 1);
        }

        // 計算平均值，避免單次挑戰中多個子項加總導致爆表
        for (String dim : mappedScores.keySet()) {
            mappedScores.put(dim, mappedScores.get(dim) / counts.get(dim));
        }

        return mappedScores;
    }
}
