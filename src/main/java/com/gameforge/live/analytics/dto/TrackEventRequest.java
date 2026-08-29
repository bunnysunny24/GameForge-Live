package com.gameforge.live.analytics.dto;

import com.gameforge.live.analytics.GameEventType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackEventRequest {

    @NotNull(message = "Event type is required")
    private GameEventType eventType;

    private String gameMode;

    private Long sessionDurationSeconds;

    private Double score;

    private Map<String, Object> metadata;
}
