package com.waterballsa.tutorial_platform.service;

import com.waterballsa.tutorial_platform.entity.*;
import com.waterballsa.tutorial_platform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService {
    private final CertificateRepository certificateRepository;
    private final GymRepository gymRepository;
    private final GymChallengeRecordRepository recordRepository;
    private final SkillRatingRepository skillRatingRepository;

    @Transactional
    public void issueCertificateIfEligible(Member member, Journey journey) {
        if (member == null || journey == null) return;

        // Check if already issued for this journey
        List<Certificate> existing = certificateRepository.findByMemberId(member.getId());
        if (existing.stream().anyMatch(c -> c.getJourney().getId().equals(journey.getId()))) {
            return;
        }

        // Check if all gyms in journey are passed
        List<Gym> gymsInJourney = journey.getGyms();
        if (gymsInJourney.isEmpty()) return;

        List<Long> gymIds = gymsInJourney.stream().map(Gym::getId).collect(Collectors.toList());
        List<GymChallengeRecord> successRecords = recordRepository.findByUserIdAndStatus(member.getId(), GymChallengeRecord.ChallengeStatus.SUCCESS);
        
        Set<Long> passedGymIds = successRecords.stream()
                .map(GymChallengeRecord::getGymId)
                .collect(Collectors.toSet());

        if (passedGymIds.containsAll(gymIds)) {
            issueCertificate(member, journey);
        }
    }

    private void issueCertificate(Member member, Journey journey) {
        String code = "CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // Snapshot skill ratings (專屬課程評分)
        SkillRatingId srId = SkillRatingId.builder()
                .memberId(member.getId())
                .journeyId(journey.getId())
                .build();
        SkillRating skillRating = skillRatingRepository.findById(srId).orElse(null);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("skillSnapshot", skillRating != null ? skillRating.getScores() : new HashMap<>());
        metadata.put("instructorName", journey.getInstructor() != null ? journey.getInstructor().getName() : "System");
        metadata.put("journeyName", journey.getName());
        metadata.put("memberDisplayName", (member.getNickName() != null && !member.getNickName().isEmpty()) ? member.getNickName() : member.getName());

        Certificate certificate = Certificate.builder()
                .member(member)
                .journey(journey)
                .verificationCode(code)
                .issuedAt(LocalDateTime.now())
                .metadata(metadata)
                .build();

        certificateRepository.save(certificate);
        log.info("Issued certificate {} to member {} for journey {}", code, member.getId(), journey.getId());
    }

    public Optional<Certificate> verifyCertificate(String code) {
        return certificateRepository.findByVerificationCode(code);
    }

    public List<Certificate> getMemberCertificates(Long memberId) {
        return certificateRepository.findByMemberId(memberId);
    }
}
