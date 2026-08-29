package com.gameforge.live.achievements.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameplayEventRequest {

    @NotBlank(message = "Metric name is required")
    private String metric; // e.g. "RACES_WON", "GAMES_PLAYED", "TOTAL_SCORE", "LEVEL_REACHED"

    @Builder.Default
    private long incrementBy = 1L;

    private Long exactValue; // Optional if event sets absolute value (like reaching Level 15)

    private Map<String, Object> metadata;
}
