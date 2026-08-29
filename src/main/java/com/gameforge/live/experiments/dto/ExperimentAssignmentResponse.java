package com.gameforge.live.experiments.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperimentAssignmentResponse {
    private Long playerId;
    private String experimentKey;
    private String assignedVariant;
    private String variantConfigJson;
    private Map<String, Object> parsedConfig;
}
