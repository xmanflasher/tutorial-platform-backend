package com.waterballsa.tutorial_platform.entity.vo; // 建議放在 vo package

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionFieldConfig implements Serializable {
    private String type;        // e.g., "image", "zip"
    private String name;        // e.g., "ooa_uml"
    private String title;       // e.g., "物件導向分析 (OOA)"
    private String description; // e.g., "上傳..."
}