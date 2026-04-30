package com.waterballsa.tutorial_platform.converter;

import com.waterballsa.tutorial_platform.dto.ChallengeDTO;
import com.waterballsa.tutorial_platform.dto.GymDetailDTO;
import com.waterballsa.tutorial_platform.dto.LessonDTO;
import com.waterballsa.tutorial_platform.entity.Gym;
import com.waterballsa.tutorial_platform.entity.LessonContent;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GymMapper {

    public GymDetailDTO toDetailDTO(Gym gym) {
        return GymDetailDTO.builder()
                .id(gym.getId())
                .name(gym.getName())
                .description(gym.getDescription())
                .challenges(gym.getChallenges() == null ? Collections.emptyList() : gym.getChallenges().stream()
                        .map(c -> ChallengeDTO.builder()
                                .id(c.getId())
                                .name(c.getName())
                                .type(c.getType())
                                .recommendDurationInDays(c.getRecommendDurationInDays())
                                .maxDurationInDays(c.getMaxDurationInDays())
                                .build())
                        .collect(Collectors.toList()))
                .lessons(gym.getRelatedLessons() == null ? Collections.<LessonDTO>emptyList() : gym.getRelatedLessons().stream()
                        .map(l -> LessonDTO.builder()
                                .id(String.valueOf(l.getId()))
                                .name(l.getName())
                                .type(l.getType())
                                .content(toContentDtoList(l.getContents()))
                                .build())
                        .collect(Collectors.toList()))
                .rewardExp(gym.getRewardExp())
                .build();
    }

    private List<java.util.Map<String, Object>> toContentDtoList(Collection<LessonContent> contents) {
        if (contents == null || contents.isEmpty()) {
            return Collections.emptyList();
        }
        return contents.stream().map(content -> java.util.Map.<String, Object>of(
                "id", content.getId(),
                "type", content.getContentType() != null ? content.getContentType().toLowerCase() : "video",
                "url", content.getUrl() != null ? content.getUrl() : "",
                "content", content.getContent() != null ? content.getContent() : ""
        )).collect(Collectors.toList());
    }
}
