package com.gameforge.live.achievements;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "achievements", indexes = {
        @Index(name = "idx_ach_metric", columnList = "target_metric")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String achievementKey;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private AchievementCategory category = AchievementCategory.PROGRESSION;

    @Column(name = "target_metric", nullable = false, length = 100)
    private String targetMetric; // e.g. "RACES_WON", "GAMES_PLAYED", "TOTAL_SCORE", "LEVEL_REACHED"

    @Column(name = "target_value", nullable = false)
    private long targetValue;

    @Builder.Default
    @Column(name = "reward_coins")
    private long rewardCoins = 500L;

    @Builder.Default
    @Column(name = "reward_xp")
    private long rewardXp = 250L;

    @Builder.Default
    @Column(name = "reward_gems")
    private int rewardGems = 0;

    @Builder.Default
    @Column(name = "badge_icon_url")
    private String badgeIconUrl = "badge_default.png";
}
