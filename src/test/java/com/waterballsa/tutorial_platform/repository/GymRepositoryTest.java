package com.waterballsa.tutorial_platform.repository;

import com.waterballsa.tutorial_platform.entity.GymChallengeRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
public class GymRepositoryTest {

    @Autowired
    private GymChallengeRecordRepository repository;

    @Test
    public void testFindByGymIdAndUserIdOrderByCreatedAtDesc() {
        // Arrange
        Long userId = 999L;
        Long gymId = 601L;
        
        GymChallengeRecord r1 = new GymChallengeRecord();
        r1.setUserId(userId);
        r1.setGymId(gymId);
        r1.setGymChallengeId(1L);
        r1.setCreatedAt(new Date(System.currentTimeMillis() - 10000)); // 10s ago
        repository.save(r1);
        
        GymChallengeRecord r2 = new GymChallengeRecord();
        r2.setUserId(userId);
        r2.setGymId(gymId);
        r2.setGymChallengeId(1L);
        r2.setCreatedAt(new Date()); // now
        repository.save(r2);

        // Act
        List<GymChallengeRecord> results = repository.findByGymIdAndUserIdOrderByCreatedAtDesc(gymId, userId);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(r2.getId(), results.get(0).getId(), "Should be ordered by created_at desc");
    }
}
