package com.waterballsa.tutorial_platform.config;

import java.util.HashMap;
import java.util.Map;

/**
 * 負責管理 Original ID (JSON) 到 Database ID (Long) 的對照表
 * 僅在 DataSeeder 執行期間使用，用完即丟。
 */
public class IdMapper {

    // Key 格式範例:
    // Journey: "J:0"
    // Mission: "M:0:5"  (Journey 0, Mission 5)
    // Chapter: "C:0:1"  (Journey 0, Chapter 1)
    // Lesson:  "L:0:1:10" (Journey 0, Chapter 1, Lesson 10)
    // Gym:     "G:0:1:10" (Journey 0, Chapter 1, Gym 10)
    private final Map<String, Long> idMap = new HashMap<>();

    // --- 1. Journey ---
    public void putJourney(Long originalId, Long dbId) {
        idMap.put("J:" + originalId, dbId);
    }
    public Long getJourney(Long originalId) {
        return idMap.get("J:" + originalId);
    }

    // --- 2. Mission (Journey scope) ---
    public void putMission(Long journeyOid, Long missionOid, Long dbId) {
        idMap.put("M:" + journeyOid + ":" + missionOid, dbId);
    }
    public Long getMission(Long journeyOid, Long missionOid) {
        return idMap.get("M:" + journeyOid + ":" + missionOid);
    }

    // --- 3. Chapter (Journey scope) ---
    public void putChapter(Long journeyOid, Long chapterOid, Long dbId) {
        idMap.put("C:" + journeyOid + ":" + chapterOid, dbId);
    }
    public Long getChapter(Long journeyOid, Long chapterOid) {
        return idMap.get("C:" + journeyOid + ":" + chapterOid);
    }

    // --- 4. Lesson (Chapter scope) ---
    public void putLesson(Long journeyOid, Long chapterOid, Long lessonOid, Long dbId) {
        idMap.put("L:" + journeyOid + ":" + chapterOid + ":" + lessonOid, dbId);
    }
    public Long getLesson(Long journeyOid, Long chapterOid, Long lessonOid) {
        return idMap.get("L:" + journeyOid + ":" + chapterOid + ":" + lessonOid);
    }

    // --- 5. Gym (Chapter scope) ---
    public void putGym(Long journeyOid, Long chapterOid, Long gymOid, Long dbId) {
        idMap.put("G:" + journeyOid + ":" + chapterOid + ":" + gymOid, dbId);
    }
    public Long getGym(Long journeyOid, Long chapterOid, Long gymOid) {
        return idMap.get("G:" + journeyOid + ":" + chapterOid + ":" + gymOid);
    }
}