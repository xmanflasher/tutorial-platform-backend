package com.waterballsa.tutorial_platform.dto.condition;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalCondition extends MissionConditionDto {
    // EXTERNAL 目前看起來沒有額外參數 (params 是空的)
    // 但為了擴充性，如果未來有 "externalUrl" 之類的可以加在這
}