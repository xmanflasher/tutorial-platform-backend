package com.waterballsa.tutorial_platform.service;

import com.waterballsa.tutorial_platform.dto.GymStatusDTO;
import com.waterballsa.tutorial_platform.entity.Gym;
import com.waterballsa.tutorial_platform.entity.GymSubmission;
import com.waterballsa.tutorial_platform.repository.GymRepository;
import com.waterballsa.tutorial_platform.repository.GymSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GymService {

    private final GymRepository gymRepository;
    private final GymSubmissionRepository submissionRepository;

    public List<GymStatusDTO> getGymMap(Long memberId) {
        // 1. 撈出所有 Gym (依照順序)
        List<Gym> allGyms = gymRepository.findAllByOrderByDisplayOrderAsc();

        // 2. 撈出該使用者的所有提交紀錄
        List<GymSubmission> submissions = submissionRepository.findByMemberId(memberId);

        List<GymStatusDTO> result = new ArrayList<>();
        boolean isPreviousPassed = true; // 第一關預設解鎖

        for (Gym gym : allGyms) {
            // 找找看有沒有這個 Gym 的提交紀錄
            GymSubmission sub = submissions.stream()
                    .filter(s -> s.getGym().getId().equals(gym.getId()))
                    .findFirst().orElse(null);

            String status = "LOCKED";
            int stars = 0;

            if (sub != null && sub.getStatus() == GymSubmission.SubmissionStatus.PASSED) {
                status = "PASSED";
                stars = sub.getGrade() != null ? sub.getGrade() : 0;
                isPreviousPassed = true;
            } else if (isPreviousPassed) {
                status = "OPEN"; // 上一關過了，這關才開啟
                isPreviousPassed = false; // 這關還沒過，下一關先鎖住
            }

            result.add(GymStatusDTO.builder()
                    .gymId(gym.getId())
                    .name(gym.getName())
                    .status(status) // LOCKED, OPEN, PASSED
                    .stars(stars)
                    .build());
        }
        return result;
    }
}