package com.waterballsa.tutorial_platform.service;

import com.waterballsa.tutorial_platform.dto.MemberDTO;
import com.waterballsa.tutorial_platform.entity.Member;
import com.waterballsa.tutorial_platform.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    // 根據 Email 獲取使用者 (用於 /api/me)
    public MemberDTO getMemberByEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Member not found: " + email));

        return MemberDTO.builder()
                .name(member.getName())
                .jobTitle(member.getJobTitle())
                .level(member.getLevel())
                .exp(member.getExp())
                .avatar(member.getAvatar())
                .email(member.getEmail())
                // .sex(member.getSex()) // 如果 Entity 有加再解開
                // .birthDate(member.getBirthDate()) // 如果 Entity 有加再解開
                .build();
    }
}