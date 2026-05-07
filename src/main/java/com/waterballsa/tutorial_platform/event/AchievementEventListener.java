package com.waterballsa.tutorial_platform.event;

import com.waterballsa.tutorial_platform.entity.*;
import com.waterballsa.tutorial_platform.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AchievementEventListener {
    private final SkillRatingService skillRatingService;
    private final CertificateService certificateService;
    private final MemberService memberService;
    private final GymService gymService;
    private final GymChallengeRecordService recordService;
    private final ChallengeService challengeService;

    @EventListener
    public void onGymPassed(GymPassedEvent event) {
        log.info("Handling GymPassedEvent for member {} and gym {}", event.getMemberId(), event.getGymId());
        
        Member member = memberService.findById(event.getMemberId()).orElse(null);
        Gym gym = gymService.findById(event.getGymId()).orElse(null);
        if (member == null || gym == null) return;

        // 2. Update Skill Ratings (only if not requested to skip)
        if (!event.isSkipSkillUpdate()) {
            // Find the successful record to get ratings
            List<GymChallengeRecord> records = recordService.getRecordsByGymAndUser(event.getGymId(), event.getMemberId());
            GymChallengeRecord successRecord = records.stream()
                    .filter(r -> r.getStatus() == GymChallengeRecord.ChallengeStatus.SUCCESS)
                    .findFirst().orElse(null);

            if (successRecord != null) {
                // [ISSUE-28-05-06-01] 僅對「實作挑戰」進行評級更新
                Challenge challenge = challengeService.findById(successRecord.getGymChallengeId()).orElse(null);
                if (challenge != null && challenge.getType() == com.waterballsa.tutorial_platform.enums.ChallengeType.PRACTICAL_CHALLENGE) {
                    skillRatingService.updateSkillRating(member, successRecord.getRatings(), successRecord.getJourneyId());
                } else {
                    log.info("[Achievement] Skip skill update for non-practical challenge (Id: {})", successRecord.getGymChallengeId());
                }
            }
        }

        // 3. Check for Certificate Eligibility
        if (gym.getJourney() != null) {
            certificateService.issueCertificateIfEligible(member, gym.getJourney());
        }
    }
}
