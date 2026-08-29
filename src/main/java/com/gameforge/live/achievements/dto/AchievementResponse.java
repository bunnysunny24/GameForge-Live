package com.gameforge.live.achievements.dto;

import com.gameforge.live.achievements.AchievementCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementResponse {
    private Long id;
    private String achievementKey;
    private String title;
    private String description;
    private AchievementCategory category;
    private String targetMetric;
    private long targetValue;
    private long rewardCoins;
    private long rewardXp;
    private int rewardGems;
    private String badgeIconUrl;
}
