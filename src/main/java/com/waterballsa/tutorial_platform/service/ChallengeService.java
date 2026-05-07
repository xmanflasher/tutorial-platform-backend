package com.waterballsa.tutorial_platform.service;

import com.waterballsa.tutorial_platform.entity.Challenge;
import com.waterballsa.tutorial_platform.repository.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChallengeService {
    private final ChallengeRepository repository;

    public Optional<Challenge> findById(Long id) {
        return repository.findById(id);
    }
}
