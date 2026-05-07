package com.waterballsa.tutorial_platform;

import com.waterballsa.tutorial_platform.entity.GymChallengeRecord;
import com.waterballsa.tutorial_platform.entity.Member;
import com.waterballsa.tutorial_platform.repository.GymChallengeRecordRepository;
import com.waterballsa.tutorial_platform.repository.MemberRepository;
import com.waterballsa.tutorial_platform.service.SkillRatingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Comparator;
import java.util.List;

/**
 * [Final Solution] 
 * 透過手動配置 DataSource，完全繞過 src/test/resources/application.properties 的 H2 干擾。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class SkillRatingSyncIT {

    @TestConfiguration
    static class ManualDbConfig {
        @Bean
        @Primary
        public DataSource dataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.postgresql.Driver");
            dataSource.setUrl("jdbc:postgresql://localhost:5432/tutorial_db");
            dataSource.setUsername("postgres");
            dataSource.setPassword("postgres");
            return dataSource;
        }
    }

    @Autowired
    private SkillRatingService skillRatingService;

    @Autowired
    private GymChallengeRecordRepository recordRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @Transactional
    @Commit
    public void syncHistoricalSkillRatings() {
        System.out.println(">>> [CRITICAL] Executing Manual Data Sync via PostgreSQL...");
        
        List<GymChallengeRecord> records = recordRepository.findAll().stream()
                .filter(r -> r.getStatus() == GymChallengeRecord.ChallengeStatus.SUCCESS)
                .filter(r -> r.getRatings() != null && !r.getRatings().isEmpty())
                .sorted(Comparator.comparing(GymChallengeRecord::getCreatedAt, 
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .toList();

        System.out.println(">>> Found " + records.size() + " records to sync.");

        int count = 0;
        for (GymChallengeRecord record : records) {
            try {
                Member member = memberRepository.findById(record.getUserId()).orElse(null);
                if (member != null) {
                    skillRatingService.updateSkillRating(member, record.getRatings());
                    count++;
                }
            } catch (Exception e) {
                System.err.println(">>> Error processing record " + record.getId() + ": " + e.getMessage());
            }
        }

        System.out.println(">>> SUCCESS: Sync Completed. Processed " + count + " records.");
    }
}
