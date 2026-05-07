package com.waterballsa.tutorial_platform.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * [Standalone Data Sync Runner]
 * 獨立於 Spring 之外的數據同步工具。直接透過 JDBC 連線執行歷史數據計算與回填。
 * 執行完畢後即可刪除此檔案。
 */
public class SkillRatingSyncRunner {

    // 資料庫連線資訊 (請根據您的本機設定調整)
    private static final String URL = "jdbc:postgresql://localhost:5432/tutorial_db";
    private static final String USER = "postgres_user";
    private static final String PASS = "postgres_password";

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Map<String, Double> GRADE_TO_SCORE = Map.of(
            "SS", 100.0, "S", 95.0, "A", 85.0, "B", 75.0, "C", 65.0, "D", 55.0, "F", 0.0
    );

    public static void main(String[] args) {
        System.out.println(">>> Starting Standalone Skill Rating Sync...");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            conn.setAutoCommit(false);
            System.out.println(">>> Connected to database: " + URL);

            // 1. 抓取所有成功挑戰紀錄
            List<Record> records = fetchRecords(conn);
            System.out.println(">>> Fetched " + records.size() + " successful records.");

            // 2. 依用戶分組並按時間排序
            Map<Long, List<Record>> userRecords = records.stream()
                    .collect(Collectors.groupingBy(r -> r.userId));

            int totalUpdated = 0;

            for (Map.Entry<Long, List<Record>> entry : userRecords.entrySet()) {
                Long userId = entry.getKey();
                List<Record> sortedRecords = entry.getValue().stream()
                        .sorted(Comparator.comparing(r -> r.createdAt))
                        .collect(Collectors.toList());

                Map<String, Double> emaScores = calculateEMA(sortedRecords);
                saveSkillRating(conn, userId, emaScores);
                totalUpdated++;
            }

            conn.commit();
            System.out.println(">>> SUCCESS! Processed " + totalUpdated + " members.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<Record> fetchRecords(Connection conn) throws Exception {
        List<Record> results = new ArrayList<>();
        // [ISSUE-28-05-06-01] 僅選取「實作挑戰 (PRACTICAL_CHALLENGE)」的成功紀錄
        String sql = "SELECT r.user_id, r.ratings, r.created_at " +
                     "FROM gym_challenge_records r " +
                     "JOIN challenges c ON r.gym_challenge_id = c.id " +
                     "WHERE r.status = 'SUCCESS' AND c.type = 'PRACTICAL_CHALLENGE'";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String ratingsJson = rs.getString("ratings");
                if (ratingsJson == null || ratingsJson.isEmpty()) continue;
                
                Map<String, String> ratings = mapper.readValue(ratingsJson, new TypeReference<Map<String, String>>() {});
                results.add(new Record(rs.getLong("user_id"), ratings, rs.getTimestamp("created_at")));
            }
        }
        return results;
    }

    private static Map<String, Double> calculateEMA(List<Record> records) {
        Map<String, Double> currentEma = new HashMap<>();
        for (Record r : records) {
            Map<String, Double> newScores = SkillMapper.mapToGeneralSkills(r.ratings, GRADE_TO_SCORE);
            for (Map.Entry<String, Double> entry : newScores.entrySet()) {
                String dim = entry.getKey();
                Double score = entry.getValue();
                Double ema = currentEma.getOrDefault(dim, score);
                currentEma.put(dim, (ema * 0.5) + (score * 0.5));
            }
        }
        return currentEma;
    }

    private static void saveSkillRating(Connection conn, Long userId, Map<String, Double> scores) throws Exception {
        String jsonScores = mapper.writeValueAsString(scores);
        String sql = "INSERT INTO skill_ratings (member_id, scores) VALUES (?, ?::jsonb) " +
                     "ON CONFLICT (member_id) DO UPDATE SET scores = EXCLUDED.scores";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.setString(2, jsonScores);
            pstmt.executeUpdate();
        }
    }

    static class Record {
        Long userId;
        Map<String, String> ratings;
        Timestamp createdAt;

        Record(Long userId, Map<String, String> ratings, Timestamp createdAt) {
            this.userId = userId;
            this.ratings = ratings;
            this.createdAt = createdAt;
        }
    }
}
