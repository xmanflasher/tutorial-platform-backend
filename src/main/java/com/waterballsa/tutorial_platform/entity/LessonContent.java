package com.waterballsa.tutorial_platform.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "lesson_contents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_type") // 對應 SQL: content_type
    private String contentType;    // e.g., "VIDEO", "MARKDOWN"

    @Column(name = "video_provider") // 對應 SQL: video_provider
    private String videoProvider;    // e.g., "YOUTUBE"

    private String url;            // 對應 SQL: url (影片連結)

    @Column(columnDefinition = "TEXT")
    private String content;        // Markdown 內容 (如果是圖文類型)

    @Column(name = "sort_order")   // 對應 SQL: sort_order
    private Integer sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id") // 對應 SQL: lesson_id
    @ToString.Exclude
    @JsonIgnore
    private Lesson lesson;
}