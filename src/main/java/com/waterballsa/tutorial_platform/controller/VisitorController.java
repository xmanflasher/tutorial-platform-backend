package com.waterballsa.tutorial_platform.controller;

import com.waterballsa.tutorial_platform.entity.VisitorLog;
import com.waterballsa.tutorial_platform.repository.VisitorLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/visitor")
@RequiredArgsConstructor
public class VisitorController {

    private final VisitorLogRepository visitorLogRepository;

    @PostMapping("/log")
    public ResponseEntity<Void> logVisitor(@RequestBody Map<String, String> payload) {
        String visitorId = payload.get("visitorId");
        String category = payload.get("category");

        if (visitorId == null || visitorId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        VisitorLog log = visitorLogRepository.findByVisitorId(visitorId)
                .orElse(VisitorLog.builder().visitorId(visitorId).build());
        
        log.setCategory(category);
        visitorLogRepository.save(log);
        
        return ResponseEntity.ok().build();
    }
}
