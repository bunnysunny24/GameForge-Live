package com.gameforge.live.liveops.dto;

import com.gameforge.live.liveops.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveEventResponse {
    private Long id;
    private String eventKey;
    private String title;
    private String description;
    private EventType eventType;
    private double multiplier;
    private Instant startTime;
    private Instant endTime;
    private boolean active;
    private boolean currentlyLive;
    private long secondsRemaining;
}
