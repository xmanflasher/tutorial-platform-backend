package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.dto.CertificateDTO;
import com.waterballsa.tutorial_platform.dto.MemberAchievementsDTO;
import com.waterballsa.tutorial_platform.entity.Certificate;
import com.waterballsa.tutorial_platform.entity.SkillRating;
import com.waterballsa.tutorial_platform.service.CertificateService;
import com.waterballsa.tutorial_platform.service.MemberService;
import com.waterballsa.tutorial_platform.service.SkillRatingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 診斷型 Controller
 * 1. 加入分段式 Try-Catch 以定位具體報錯服務
 * 2. 實作「容錯降級」，避免因單一資料庫報錯導致整個 API 500
 */
@Slf4j
@RestController
@RequestMapping("/members/me/achievements")
@RequiredArgsConstructor
public class MemberAchievementController {
    private final SkillRatingService skillRatingService;
    private final CertificateService certificateService;
    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<MemberAchievementsDTO> getMyAchievements(
            Authentication auth,
            @RequestParam(required = false) Long journeyId) {
        Long memberId = memberService.getCurrentMemberId(auth);
        if (memberId == null) {
            log.info("[Achievement] User not logged in, using diagnostic memberId=1");
            memberId = 1L;
        }

        log.info("[Achievement] Starting diagnostic fetch for memberId={} (JourneyId={})", memberId, journeyId);
        
        // 分段處理 1: Skill Rating (根據傳入的 journeyId 獲取，預設 0L 為綜合)
        Long targetJourneyId = (journeyId != null) ? journeyId : 0L;
        Map<String, Double> skillScores = new HashMap<>();
        try {
            SkillRating skillRating = skillRatingService.getSkillRating(memberId, targetJourneyId);
            if (skillRating != null && skillRating.getScores() != null) {
                skillScores = skillRating.getScores();
            }
        } catch (Exception e) {
            log.error("[Achievement] Critical: SkillRating service failed!", e);
            // 保持 empty map，不報 500
        }

        // 分段處理 2: Certificates
        List<CertificateDTO> certDtos = new ArrayList<>();
        try {
            List<Certificate> certificates = certificateService.getMemberCertificates(memberId);
            if (certificates != null) {
                certDtos = certificates.stream()
                        .map(this::toDTO)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("[Achievement] Critical: Certificate service failed!", e);
        }

        return ResponseEntity.ok(MemberAchievementsDTO.builder()
                .skillRating(skillScores)
                .certificates(certDtos)
                .build());
    }

    private CertificateDTO toDTO(Certificate cert) {
        try {
            return CertificateDTO.builder()
                    .id(cert.getId())
                    .verificationCode(cert.getVerificationCode())
                    .issuedAt(cert.getIssuedAt())
                    .metadata(cert.getMetadata() != null ? cert.getMetadata() : new HashMap<>())
                    .build();
        } catch (Exception e) {
            log.error("[Achievement] Error mapping certificate ID: {} to DTO", cert.getId(), e);
            return CertificateDTO.builder().id(cert.getId()).verificationCode("ERROR").build();
        }
    }
}
