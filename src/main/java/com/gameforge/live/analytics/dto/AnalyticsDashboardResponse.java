package com.gameforge.live.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsDashboardResponse {
    private long totalEventsTracked;
    private long totalGamesPlayed;
    private long dailyActiveUsers;
    private double averageSessionDurationMinutes;
    private String mostPopularGameMode;
    private Map<String, Long> gameModeDistribution;
    private Map<String, Long> eventTypeCounts;
    private double liveEventParticipationRate;
}
