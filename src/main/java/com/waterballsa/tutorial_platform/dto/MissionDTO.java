package com.waterballsa.tutorial_platform.dto;

import java.util.List;
import java.util.ArrayList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.waterballsa.tutorial_platform.dto.condition.MissionConditionDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissionDTO {
    private String id;
    private String name;
    private String description;
    private Integer durationInDays;
    private RewardDTO reward;
    // 建議：直接回傳完整的 requirements list
    private List<MissionConditionDto> requirements;

    // 或者，如果你想維持 API 格式不變 (對前端友善)，
    // 你可以在 Mapper 層把 requirements 拆開塞進去：
    private List<MissionConditionDto> prerequisites;
    private List<MissionConditionDto> criteria;
}