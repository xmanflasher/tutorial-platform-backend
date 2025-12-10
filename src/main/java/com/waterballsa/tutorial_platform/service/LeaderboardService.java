package com.waterballsa.tutorial_platform.service;

import com.waterballsa.tutorial_platform.dto.LeaderboardDTO;
import com.waterballsa.tutorial_platform.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final MemberRepository memberRepository;

    public List<LeaderboardDTO> getLeaderboard() {
        return memberRepository.findTop100ByOrderByExpDesc().stream()
                .map(m -> LeaderboardDTO.builder()
                        .id(m.getId())
                        .name(m.getName())
                        .avatar(m.getAvatar())
                        .exp(m.getExp())
                        .level(m.getLevel())
                        .jobTitle(m.getJobTitle())
                        .build())
                .collect(Collectors.toList());
    }
}