package com.gameforge.live.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameforge.live.achievements.Achievement;
import com.gameforge.live.achievements.AchievementCategory;
import com.gameforge.live.achievements.AchievementRepository;
import com.gameforge.live.analytics.GameEvent;
import com.gameforge.live.analytics.GameEventRepository;
import com.gameforge.live.analytics.GameEventType;
import com.gameforge.live.experiments.Experiment;
import com.gameforge.live.experiments.ExperimentRepository;
import com.gameforge.live.experiments.ExperimentVariant;
import com.gameforge.live.featureflags.FeatureFlag;
import com.gameforge.live.featureflags.FeatureFlagRepository;
import com.gameforge.live.leaderboard.LeaderboardService;
import com.gameforge.live.leaderboard.dto.ScoreSubmissionRequest;
import com.gameforge.live.liveops.EventType;
import com.gameforge.live.liveops.LiveEvent;
import com.gameforge.live.liveops.LiveEventRepository;
import com.gameforge.live.player.Player;
import com.gameforge.live.player.PlayerRepository;
import com.gameforge.live.security.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;
    private final LiveEventRepository liveEventRepository;
    private final AchievementRepository achievementRepository;
    private final FeatureFlagRepository featureFlagRepository;
    private final ExperimentRepository experimentRepository;
    private final GameEventRepository gameEventRepository;
    private final LeaderboardService leaderboardService;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        if (playerRepository.count() > 0) {
            log.info("Database already initialized, skipping seed data.");
            return;
        }

        log.info("⚡ Seeding GameForge-Live platform data...");

        // 1. Create Admin
        Player admin = playerRepository.save(Player.builder()
                .username("admin")
                .email("admin@gameforge.live")
                .password(passwordEncoder.encode("Admin@123"))
                .role(Role.ROLE_ADMIN)
                .level(99)
                .xp(999999L)
                .coins(1000000L)
                .gems(9999)
                .avatarUrl("avatar_admin.png")
                .createdAt(Instant.now().minus(30, ChronoUnit.DAYS))
                .lastLoginAt(Instant.now())
                .enabled(true)
                .build());

        // 2. Create Players
        Player p1 = playerRepository.save(Player.builder()
                .username("Bhavashesh")
                .email("bhavashesh@gameforge.live")
                .password(passwordEncoder.encode("Password@123"))
                .role(Role.ROLE_PLAYER)
                .level(15)
                .xp(8500L)
                .coins(12000L)
                .gems(150)
                .avatarUrl("avatar_bhavashesh.png")
                .createdAt(Instant.now().minus(10, ChronoUnit.DAYS))
                .lastLoginAt(Instant.now())
                .enabled(true)
                .build());

        Player p2 = playerRepository.save(Player.builder()
                .username("CyberKnight")
                .email("cyberknight@gameforge.live")
                .password(passwordEncoder.encode("Password@123"))
                .role(Role.ROLE_PLAYER)
                .level(18)
                .xp(12500L)
                .coins(18000L)
                .gems(200)
                .avatarUrl("avatar_cyberknight.png")
                .createdAt(Instant.now().minus(15, ChronoUnit.DAYS))
                .lastLoginAt(Instant.now())
                .enabled(true)
                .build());

        Player p3 = playerRepository.save(Player.builder()
                .username("ViperStrike")
                .email("viper@gameforge.live")
                .password(passwordEncoder.encode("Password@123"))
                .role(Role.ROLE_PLAYER)
                .level(12)
                .xp(6200L)
                .coins(9500L)
                .gems(80)
                .avatarUrl("avatar_viper.png")
                .createdAt(Instant.now().minus(8, ChronoUnit.DAYS))
                .lastLoginAt(Instant.now())
                .enabled(true)
                .build());

        Player p4 = playerRepository.save(Player.builder()
                .username("PixelMaster")
                .email("pixel@gameforge.live")
                .password(passwordEncoder.encode("Password@123"))
                .role(Role.ROLE_PLAYER)
                .level(8)
                .xp(3100L)
                .coins(4200L)
                .gems(30)
                .avatarUrl("avatar_pixel.png")
                .createdAt(Instant.now().minus(5, ChronoUnit.DAYS))
                .lastLoginAt(Instant.now())
                .enabled(true)
                .build());

        // 3. Seed Live Events
        Instant now = Instant.now();
        liveEventRepository.save(LiveEvent.builder()
                .eventKey("WEEKEND_DOUBLE_XP")
                .title("🎉 Weekend Double XP Extravaganza")
                .description("Earn 2x XP for all race completions and mission milestones this entire weekend!")
                .eventType(EventType.DOUBLE_XP)
                .multiplier(2.0)
                .startTime(now.minus(12, ChronoUnit.HOURS))
                .endTime(now.plus(48, ChronoUnit.HOURS))
                .active(true)
                .build());

        liveEventRepository.save(LiveEvent.builder()
                .eventKey("COIN_FRENZY")
                .title("💰 Double Coin Frenzy")
                .description("All match coin payouts doubled!")
                .eventType(EventType.DOUBLE_COINS)
                .multiplier(2.0)
                .startTime(now.minus(6, ChronoUnit.HOURS))
                .endTime(now.plus(24, ChronoUnit.HOURS))
                .active(true)
                .build());

        // 4. Seed Achievements
        achievementRepository.save(Achievement.builder()
                .achievementKey("SPEED_DEMON")
                .title("🏎️ Speed Demon")
                .description("Win 10 competitive races")
                .category(AchievementCategory.RACING)
                .targetMetric("RACES_WON")
                .targetValue(10L)
                .rewardCoins(1000L)
                .rewardXp(500L)
                .rewardGems(10)
                .badgeIconUrl("badge_speed_demon.png")
                .build());

        achievementRepository.save(Achievement.builder()
                .achievementKey("VETERAN")
                .title("🎖️ Veteran Pilot")
                .description("Complete 100 multiplayer matches")
                .category(AchievementCategory.PROGRESSION)
                .targetMetric("GAMES_PLAYED")
                .targetValue(100L)
                .rewardCoins(5000L)
                .rewardXp(2500L)
                .rewardGems(50)
                .badgeIconUrl("badge_veteran.png")
                .build());

        achievementRepository.save(Achievement.builder()
                .achievementKey("HIGH_SCORER")
                .title("🔥 High Scorer")
                .description("Accumulate a single score above 50,000 points")
                .category(AchievementCategory.PROGRESSION)
                .targetMetric("TOTAL_SCORE")
                .targetValue(50000L)
                .rewardCoins(2000L)
                .rewardXp(1000L)
                .rewardGems(25)
                .badgeIconUrl("badge_high_scorer.png")
                .build());

        achievementRepository.save(Achievement.builder()
                .achievementKey("LEVEL_MASTER")
                .title("👑 Master Ascendant")
                .description("Reach Level 15 in player progression")
                .category(AchievementCategory.PROGRESSION)
                .targetMetric("LEVEL_REACHED")
                .targetValue(15L)
                .rewardCoins(3000L)
                .rewardXp(1500L)
                .rewardGems(30)
                .badgeIconUrl("badge_master.png")
                .build());

        // 5. Seed Feature Flags
        featureFlagRepository.save(FeatureFlag.builder()
                .flagKey("NEW_RACING_MODE")
                .description("Enables 60fps Hyper-Drift Racing Track Alpha")
                .enabled(true)
                .rolloutPercentage(25)
                .whitelistedPlayerIds(p1.getId().toString())
                .updatedAt(now)
                .build());

        featureFlagRepository.save(FeatureFlag.builder()
                .flagKey("DOUBLE_REWARDS_VIP")
                .description("VIP Daily Login 2x reward booster")
                .enabled(true)
                .rolloutPercentage(50)
                .updatedAt(now)
                .build());

        featureFlagRepository.save(FeatureFlag.builder()
                .flagKey("BETA_VOICE_CHAT")
                .description("Low-latency spatial audio party chat")
                .enabled(true)
                .rolloutPercentage(10)
                .updatedAt(now)
                .build());

        // 6. Seed A/B Experiment
        List<ExperimentVariant> variants = List.of(
                ExperimentVariant.builder().name("CONTROL").weight(50).configJson("{\"rewardCoins\":100,\"bonusGems\":0}").build(),
                ExperimentVariant.builder().name("TREATMENT_BOOSTED").weight(50).configJson("{\"rewardCoins\":200,\"bonusGems\":5}").build()
        );
        experimentRepository.save(Experiment.builder()
                .experimentKey("DAILY_REWARD_EXP_V1")
                .description("Testing 100 vs 200 daily login coins retention impact")
                .variantsJson(objectMapper.writeValueAsString(variants))
                .active(true)
                .updatedAt(now)
                .build());

        // 7. Seed Scores into Leaderboard
        leaderboardService.submitScore(p2.getId(), ScoreSubmissionRequest.builder().leaderboardName("GLOBAL").score(15500.0).gameMode("RACING").build());
        leaderboardService.submitScore(p1.getId(), ScoreSubmissionRequest.builder().leaderboardName("GLOBAL").score(12000.0).gameMode("RACING").build());
        leaderboardService.submitScore(p3.getId(), ScoreSubmissionRequest.builder().leaderboardName("GLOBAL").score(9800.0).gameMode("RACING").build());
        leaderboardService.submitScore(p4.getId(), ScoreSubmissionRequest.builder().leaderboardName("GLOBAL").score(5400.0).gameMode("RACING").build());

        // 8. Seed Game Events for Telemetry & Analytics Dashboard
        gameEventRepository.save(GameEvent.builder().playerId(p1.getId()).eventType(GameEventType.PLAYER_LOGIN).sessionDurationSeconds(0L).timestamp(now.minus(4, ChronoUnit.HOURS)).build());
        gameEventRepository.save(GameEvent.builder().playerId(p1.getId()).eventType(GameEventType.GAME_STARTED).gameMode("RACING").timestamp(now.minus(3, ChronoUnit.HOURS)).build());
        gameEventRepository.save(GameEvent.builder().playerId(p1.getId()).eventType(GameEventType.GAME_COMPLETED).gameMode("RACING").sessionDurationSeconds(1080L).score(12000.0).timestamp(now.minus(2, ChronoUnit.HOURS)).build());
        gameEventRepository.save(GameEvent.builder().playerId(p1.getId()).eventType(GameEventType.EVENT_PARTICIPATED).gameMode("WEEKEND_DOUBLE_XP").timestamp(now.minus(2, ChronoUnit.HOURS)).build());

        gameEventRepository.save(GameEvent.builder().playerId(p2.getId()).eventType(GameEventType.PLAYER_LOGIN).sessionDurationSeconds(0L).timestamp(now.minus(5, ChronoUnit.HOURS)).build());
        gameEventRepository.save(GameEvent.builder().playerId(p2.getId()).eventType(GameEventType.GAME_COMPLETED).gameMode("RACING").sessionDurationSeconds(1200L).score(15500.0).timestamp(now.minus(3, ChronoUnit.HOURS)).build());

        gameEventRepository.save(GameEvent.builder().playerId(p3.getId()).eventType(GameEventType.PLAYER_LOGIN).sessionDurationSeconds(0L).timestamp(now.minus(6, ChronoUnit.HOURS)).build());
        gameEventRepository.save(GameEvent.builder().playerId(p3.getId()).eventType(GameEventType.GAME_COMPLETED).gameMode("BATTLE_ROYALE").sessionDurationSeconds(840L).score(9800.0).timestamp(now.minus(4, ChronoUnit.HOURS)).build());
        gameEventRepository.save(GameEvent.builder().playerId(p3.getId()).eventType(GameEventType.LEVEL_FAILED).gameMode("BATTLE_ROYALE").sessionDurationSeconds(300L).timestamp(now.minus(1, ChronoUnit.HOURS)).build());

        log.info("✅ GameForge-Live platform seed data successfully initialized!");
    }
}
