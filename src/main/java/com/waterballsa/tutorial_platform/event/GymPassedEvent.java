package com.waterballsa.tutorial_platform.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 領域事件：道館通關成功
 */
@Getter
@AllArgsConstructor
public class GymPassedEvent {
    private final Long memberId;
    private final Long gymId;
}
