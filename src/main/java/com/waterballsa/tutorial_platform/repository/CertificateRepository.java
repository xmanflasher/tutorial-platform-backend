package com.waterballsa.tutorial_platform.repository;

import com.waterballsa.tutorial_platform.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    Optional<Certificate> findByVerificationCode(String verificationCode);
    List<Certificate> findByMemberId(Long memberId);
}
