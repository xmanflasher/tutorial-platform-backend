package com.waterballsa.tutorial_platform.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChapterDTO {
    private String id;
    private String name;
    private List<LessonDTO> lessons; // 對應到下方的 LessonDTO

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<LessonDTO> getLessons() { return lessons; }
    public void setLessons(List<LessonDTO> lessons) { this.lessons = lessons; }
}