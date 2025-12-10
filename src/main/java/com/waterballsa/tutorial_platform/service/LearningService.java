package com.waterballsa.tutorial_platform.service;

import com.waterballsa.tutorial_platform.entity.LearningRecord;
import com.waterballsa.tutorial_platform.entity.Lesson;
import com.waterballsa.tutorial_platform.entity.Member;
import com.waterballsa.tutorial_platform.repository.LearningRecordRepository;
import com.waterballsa.tutorial_platform.repository.LessonRepository;
import com.waterballsa.tutorial_platform.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LearningService {

    private final LearningRecordRepository recordRepo;
    private final MemberRepository memberRepo;

    // ★★★ 修正 1: 補上這個宣告，Spring 才會注入 lessonRepository ★★★
    private final LessonRepository lessonRepository;

    @Transactional
    public void markLessonAsComplete(Long memberId, Long lessonId) {
        // 1. 檢查是否已完成 (避免重複刷經驗)
        if (recordRepo.existsByMemberIdAndLessonId(memberId, lessonId)) {
            return;
        }

        // 2. 建立紀錄
        Member member = memberRepo.getReferenceById(memberId);
        Lesson lesson = lessonRepository.getReferenceById(lessonId);

        LearningRecord record = LearningRecord.builder()
                .member(member)
                .lesson(lesson)
                .finished(true)
                .build();
        recordRepo.save(record);

        // 3. 發放獎勵
        if (lesson.getReward() != null) {
            // ★★★ 修正 2 的前提：Member 必須要有 getExp/getCoin 方法
            // 為了避免 NullPointerException，建議先判斷是否為 null，或在 Entity 設定預設值
            long currentExp = member.getExp() == null ? 0 : member.getExp();
            long currentCoin = member.getCoin() == null ? 0 : member.getCoin();

            member.setExp(currentExp + lesson.getReward().getExp());
            member.setCoin(currentCoin + lesson.getReward().getCoin());

            memberRepo.save(member);
        }
    }
}