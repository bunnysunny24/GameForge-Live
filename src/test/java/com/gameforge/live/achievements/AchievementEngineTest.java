package com.gameforge.live.achievements;

import com.gameforge.live.achievements.dto.AchievementUnlockNotification;
import com.gameforge.live.achievements.dto.GameplayEventRequest;
import com.gameforge.live.player.Player;
import com.gameforge.live.player.PlayerRepository;
import com.gameforge.live.player.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AchievementEngineTest {

    @Mock
    private AchievementRepository achievementRepository;

    @Mock
    private PlayerAchievementRepository playerAchievementRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerService playerService;

    @InjectMocks
    private AchievementEngine achievementEngine;

    private Player player;
    private Achievement speedDemonAchievement;

    @BeforeEach
    void setUp() {
        player = Player.builder().id(1L).username("SpeedRacer").xp(1000L).coins(500L).gems(10).build();
        speedDemonAchievement = Achievement.builder()
                .id(10L)
                .achievementKey("SPEED_DEMON")
                .title("Speed Demon")
                .targetMetric("RACES_WON")
                .targetValue(10L)
                .rewardCoins(1000L)
                .rewardXp(500L)
                .rewardGems(5)
                .build();
    }

    @Test
    void processGameplayEvent_ProgressIncreases_UnlocksWhenReachingTarget() {
        PlayerAchievement existingProgress = PlayerAchievement.builder()
                .id(100L)
                .player(player)
                .achievement(speedDemonAchievement)
                .currentProgress(9L) // 1 race left to unlock!
                .unlocked(false)
                .build();

        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(achievementRepository.findByTargetMetric("RACES_WON")).thenReturn(List.of(speedDemonAchievement));
        when(playerAchievementRepository.findByPlayerIdAndAchievementId(1L, 10L)).thenReturn(Optional.of(existingProgress));
        when(playerAchievementRepository.save(any(PlayerAchievement.class))).thenAnswer(i -> i.getArgument(0));

        GameplayEventRequest request = GameplayEventRequest.builder()
                .metric("RACES_WON")
                .incrementBy(1L)
                .build();

        AchievementUnlockNotification notification = achievementEngine.processGameplayEvent(1L, request);

        assertNotNull(notification);
        assertEquals("RACES_WON", notification.getMetricProcessed());
        assertEquals(10L, notification.getNewMetricValue());
        assertEquals(1, notification.getNewlyUnlockedAchievements().size());
        assertEquals("SPEED_DEMON", notification.getNewlyUnlockedAchievements().get(0).getAchievementKey());
        assertEquals(1000L, notification.getTotalCoinsAwarded());
        assertEquals(500L, notification.getTotalXpAwarded());
        assertEquals(5, notification.getTotalGemsAwarded());

        verify(playerService).addCurrency(1L, 1000L, 5);
        verify(playerAchievementRepository).save(any(PlayerAchievement.class));
    }
}
