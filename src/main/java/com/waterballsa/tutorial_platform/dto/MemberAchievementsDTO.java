package com.waterballsa.tutorial_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberAchievementsDTO {
    private Map<String, Double> skillRating;
    private List<CertificateDTO> certificates;
}
