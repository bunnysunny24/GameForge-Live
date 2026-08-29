package com.gameforge.live.achievements.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerAchievementResponse {
    private Long achievementId;
    private String achievementKey;
    private String title;
    private String description;
    private String category;
    private String targetMetric;
    private long targetValue;
    private long currentProgress;
    private double progressPercentage;
    private boolean unlocked;
    private Instant unlockedAt;
    private long rewardCoins;
    private long rewardXp;
    private int rewardGems;
    private String badgeIconUrl;
}
