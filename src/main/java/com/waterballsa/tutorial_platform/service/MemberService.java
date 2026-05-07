package com.waterballsa.tutorial_platform.service;

import com.waterballsa.tutorial_platform.dto.InstructorProfileUpdateDTO;
import com.waterballsa.tutorial_platform.dto.MemberDTO;
import com.waterballsa.tutorial_platform.dto.MemberUpdateDTO;
import com.waterballsa.tutorial_platform.entity.Member;
import com.waterballsa.tutorial_platform.exception.BusinessException;
import com.waterballsa.tutorial_platform.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public Long getCurrentMemberId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        String email = null;

        if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User oAuth2User) {
            email = oAuth2User.getAttribute("email");
        } else if (principal instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
            email = jwt.getClaim("email");
        } else if (principal instanceof String s) {
            email = s;
        } else {
            email = authentication.getName();
        }

        if (email == null) return null;
        return memberRepository.findByEmail(email)
                .map(Member::getId)
                .orElse(null);
    }

    public MemberDTO getMemberDto(Long memberId) {
        if (memberId == null) {
            return MemberDTO.builder().role("ROLE_GUEST").name("Guest").build();
        }

        return memberRepository.findById(memberId)
                .map(this::toDTO)
                .orElse(MemberDTO.builder().role("ROLE_USER").name("New Student").build());
    }

    public List<MemberDTO> getMembersByIds(List<Long> ids) {
        return memberRepository.findByIdIn(ids)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public MemberDTO updateMemberProfile(Long memberId, MemberUpdateDTO updateDTO) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException("Member not found with id: " + memberId));

        if (updateDTO.getNickName() != null) member.setNickName(updateDTO.getNickName());
        if (updateDTO.getAvatar() != null) member.setAvatar(updateDTO.getAvatar());
        if (updateDTO.getJobTitle() != null) member.setJobTitle(updateDTO.getJobTitle());
        if (updateDTO.getRegion() != null) member.setRegion(updateDTO.getRegion());
        if (updateDTO.getOccupation() != null) member.setOccupation(updateDTO.getOccupation());

        return toDTO(memberRepository.save(member));
    }

    @Transactional
    public MemberDTO updateInstructorProfile(Long memberId, InstructorProfileUpdateDTO updateDTO) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException("Member not found"));

        if (updateDTO.getInstructorBio() != null) member.setInstructorBio(updateDTO.getInstructorBio());
        if (updateDTO.getSocialLinks() != null) member.setSocialLinks(updateDTO.getSocialLinks());
        if (updateDTO.getJobTitle() != null) member.setJobTitle(updateDTO.getJobTitle());

        return toDTO(memberRepository.save(member));
    }

    @Transactional
    public MemberDTO updateMember(Long memberId, MemberDTO dto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException("Member not found"));

        if (dto.getNickName() != null) member.setNickName(dto.getNickName());
        if (dto.getJobTitle() != null) member.setJobTitle(dto.getJobTitle());
        if (dto.getOccupation() != null) member.setOccupation(dto.getOccupation());
        if (dto.getSex() != null) member.setSex(dto.getSex());
        if (dto.getBirthDate() != null) member.setBirthDate(dto.getBirthDate());
        if (dto.getRegion() != null) member.setRegion(dto.getRegion());
        if (dto.getGithubUrl() != null) member.setGithubUrl(dto.getGithubUrl());
        if (dto.getDiscordId() != null) member.setDiscordId(dto.getDiscordId());

        return toDTO(memberRepository.save(member));
    }

    public Member getMemberById(Long id) {
        return memberRepository.findById(id).orElse(null);
    }

    public java.util.Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    private MemberDTO toDTO(Member member) {
        return MemberDTO.builder()
                .id(member.getId())
                .name(member.getName())
                .nickName(member.getNickName())
                .jobTitle(member.getJobTitle())
                .occupation(member.getOccupation())
                .level(member.getLevel())
                .exp(member.getExp())
                .nextLevelExp(member.getNextLevelExp())
                .avatar(member.getAvatar())
                .pictureUrl(member.getAvatar())
                .email(member.getEmail())
                .sex(member.getSex())
                .birthDate(member.getBirthDate())
                .region(member.getRegion())
                .githubUrl(member.getGithubUrl())
                .discordId(member.getDiscordId())
                .role(member.getRole() != null ? member.getRole().name() : null)
                .instructorBio(member.getInstructorBio())
                .socialLinks(member.getSocialLinks())
                .build();
    }
}