package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.dto.CertificateDTO;
import com.waterballsa.tutorial_platform.entity.Certificate;
import com.waterballsa.tutorial_platform.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/certificates")
@RequiredArgsConstructor
public class CertificateController {
    private final CertificateService certificateService;

    @GetMapping("/verify/{code}")
    public ResponseEntity<CertificateDTO> verifyCertificate(@PathVariable String code) {
        return certificateService.verifyCertificate(code)
                .map(this::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private CertificateDTO toDTO(Certificate cert) {
        return CertificateDTO.builder()
                .id(cert.getId())
                .verificationCode(cert.getVerificationCode())
                .issuedAt(cert.getIssuedAt())
                .metadata(cert.getMetadata())
                .build();
    }
}
