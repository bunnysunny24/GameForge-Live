package com.gameforge.live.achievements.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementUnlockNotification {
    private String metricProcessed;
    private long newMetricValue;
    private List<PlayerAchievementResponse> newlyUnlockedAchievements;
    private long totalCoinsAwarded;
    private long totalXpAwarded;
    private int totalGemsAwarded;
}
