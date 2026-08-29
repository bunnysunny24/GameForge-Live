package com.gameforge.live.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerAnalyticsSummary {
    private Long playerId;
    private long totalSessions;
    private long totalPlaytimeSeconds;
    private double averageSessionMinutes;
    private long gamesCompleted;
    private long gamesFailed;
    private double winRatePercentage;
    private List<String> recentActivity;
}
