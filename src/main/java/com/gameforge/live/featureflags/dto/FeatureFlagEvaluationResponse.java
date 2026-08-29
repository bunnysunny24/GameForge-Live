package com.gameforge.live.featureflags.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureFlagEvaluationResponse {
    private Long playerId;
    private Map<String, Boolean> features;
    private Map<String, String> evaluationReasons; // e.g. "WHITELISTED", "PERCENTAGE_ROLLOUT", "GLOBAL_DISABLED"
}
