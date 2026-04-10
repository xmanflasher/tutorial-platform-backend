package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.entity.*;
import com.waterballsa.tutorial_platform.event.GymPassedEvent;
import com.waterballsa.tutorial_platform.repository.*;
import com.waterballsa.tutorial_platform.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class DemoController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final MemberMissionRepository memberMissionRepository;
    private final GymRepository gymRepository;
    private final GymSubmissionRepository gymSubmissionRepository;
    private final GymChallengeRecordRepository gymChallengeRecordRepository;
    private final GymBadgeRepository gymBadgeRepository;
    private final MemberBadgeRepository memberBadgeRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 完成當前正在進行中的任務
     */
    @PostMapping("/complete-current-mission")
    @Transactional
    public ResponseEntity<String> completeCurrentMission(Authentication auth) {
        Long memberId = memberService.getCurrentMemberId(auth);
        if (memberId == null) return ResponseEntity.status(401).body("Unauthorized");

        // 找尋該使用者進行中的任務
        List<MemberMission> inProgressMissions = memberMissionRepository.findAllByMember_Id(memberId).stream()
                .filter(m -> m.getStatus() == MemberMission.MissionStatus.IN_PROGRESS)
                .toList();

        if (inProgressMissions.isEmpty()) {
            return ResponseEntity.badRequest().body("您當前沒有進行中的任務");
        }

        // 取第一筆進行中的任務將其完成
        MemberMission mission = inProgressMissions.get(0);
        mission.setStatus(MemberMission.MissionStatus.COMPLETED);
        mission.setCompletedAt(LocalDateTime.now());
        mission.setCurrentProgress(100);
        memberMissionRepository.save(mission);

        log.info("Demo: Member {} completed mission {}", memberId, mission.getMission().getName());
        return ResponseEntity.ok("任務「" + mission.getMission().getName() + "」已模擬完成！");
    }

    /**
     * 完成地圖上下一個道館
     */
    @PostMapping("/complete-current-gym")
    @Transactional
    public ResponseEntity<String> completeCurrentGym(Authentication auth) {
        Long memberId = memberService.getCurrentMemberId(auth);
        if (memberId == null) return ResponseEntity.status(401).body("Unauthorized");

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        // 1. 取得所有道館
        List<Gym> allGyms = gymRepository.findAllByOrderByDisplayOrderAsc();
        
        // 2. 取得已通過的道館 ID
        List<Long> passedGymIds = gymSubmissionRepository.findByMemberId(memberId).stream()
                .filter(s -> s.getStatus() == GymSubmission.SubmissionStatus.SUCCESS)
                .map(s -> s.getGym().getId())
                .toList();

        // 3. 找尋第一個尚未通過的道館
        Optional<Gym> nextGymOpt = allGyms.stream()
                .filter(g -> !passedGymIds.contains(g.getId()))
                .findFirst();

        if (nextGymOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("您已通關所有道館！");
        }

        Gym gym = nextGymOpt.get();

        // 4. 建立 GymSubmission (SUCCESS)
        GymSubmission submission = GymSubmission.builder()
                .member(member)
                .gym(gym)
                .challengeId(gym.getChallenges() != null && !gym.getChallenges().isEmpty() ? gym.getChallenges().get(0).getId() : 1L)
                .status(GymSubmission.SubmissionStatus.SUCCESS)
                .grade(3) // 預設 3 星
                .submittedAt(LocalDateTime.now())
                .build();
        gymSubmissionRepository.save(submission);

        Long journeyId = gym.getJourney() != null ? gym.getJourney().getId() : null;

        // 5. 建立 GymChallengeRecord (SUCCESS)
        GymChallengeRecord record = GymChallengeRecord.builder()
                .userId(memberId)
                .gymId(gym.getId())
                .journeyId(journeyId)
                .gymChallengeId(submission.getChallengeId())
                .status(GymChallengeRecord.ChallengeStatus.SUCCESS)
                .createdAt(new Date())
                .completedAt(new Date())
                .build();
        gymChallengeRecordRepository.save(record);

        // 6. 發佈通關事件，啟動解鎖引擎 (ISSUE-BADGE-01)
        eventPublisher.publishEvent(new GymPassedEvent(memberId, gym.getId()));

        log.info("Demo: Member {} completed gym {}", memberId, gym.getName());
        return ResponseEntity.ok("道館「" + gym.getName() + "」已模擬成功，系統正自動計算獎勵與徽章！");
    }
}
