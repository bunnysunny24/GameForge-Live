package com.gameforge.live.achievements;

import com.gameforge.live.achievements.dto.AchievementResponse;
import com.gameforge.live.achievements.dto.AchievementUnlockNotification;
import com.gameforge.live.achievements.dto.GameplayEventRequest;
import com.gameforge.live.achievements.dto.PlayerAchievementResponse;
import com.gameforge.live.common.ResourceNotFoundException;
import com.gameforge.live.player.Player;
import com.gameforge.live.player.PlayerRepository;
import com.gameforge.live.player.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementEngine {

    private final AchievementRepository achievementRepository;
    private final PlayerAchievementRepository playerAchievementRepository;
    private final PlayerRepository playerRepository;
    private final PlayerService playerService;

    public List<AchievementResponse> getAllAchievements() {
        return achievementRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PlayerAchievementResponse> getPlayerAchievements(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with ID: " + playerId));

        List<Achievement> allAchievements = achievementRepository.findAll();
        Map<Long, PlayerAchievement> existingProgress = playerAchievementRepository
                .findAllByPlayerIdWithAchievement(playerId).stream()
                .collect(Collectors.toMap(pa -> pa.getAchievement().getId(), pa -> pa));

        return allAchievements.stream()
                .map(ach -> {
                    PlayerAchievement pa = existingProgress.get(ach.getId());
                    long progress = pa != null ? pa.getCurrentProgress() : 0L;
                    boolean unlocked = pa != null && pa.isUnlocked();
                    Instant unlockedAt = pa != null ? pa.getUnlockedAt() : null;

                    double percentage = Math.min(100.0, ((double) progress / (double) ach.getTargetValue()) * 100.0);

                    return PlayerAchievementResponse.builder()
                            .achievementId(ach.getId())
                            .achievementKey(ach.getAchievementKey())
                            .title(ach.getTitle())
                            .description(ach.getDescription())
                            .category(ach.getCategory().name())
                            .targetMetric(ach.getTargetMetric())
                            .targetValue(ach.getTargetValue())
                            .currentProgress(progress)
                            .progressPercentage(Math.round(percentage * 10.0) / 10.0)
                            .unlocked(unlocked)
                            .unlockedAt(unlockedAt)
                            .rewardCoins(ach.getRewardCoins())
                            .rewardXp(ach.getRewardXp())
                            .rewardGems(ach.getRewardGems())
                            .badgeIconUrl(ach.getBadgeIconUrl())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public AchievementUnlockNotification processGameplayEvent(Long playerId, GameplayEventRequest request) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with ID: " + playerId));

        String metric = request.getMetric().toUpperCase().trim();
        List<Achievement> relevantAchievements = achievementRepository.findByTargetMetric(metric);

        List<PlayerAchievementResponse> newlyUnlocked = new ArrayList<>();
        long totalCoinsAwarded = 0L;
        long totalXpAwarded = 0L;
        int totalGemsAwarded = 0;
        long finalValue = 0L;

        for (Achievement ach : relevantAchievements) {
            PlayerAchievement pa = playerAchievementRepository
                    .findByPlayerIdAndAchievementId(playerId, ach.getId())
                    .orElseGet(() -> PlayerAchievement.builder()
                            .player(player)
                            .achievement(ach)
                            .currentProgress(0L)
                            .unlocked(false)
                            .build());

            if (pa.isUnlocked()) {
                finalValue = pa.getCurrentProgress();
                continue;
            }

            long newProgress;
            if (request.getExactValue() != null) {
                newProgress = request.getExactValue();
            } else {
                newProgress = pa.getCurrentProgress() + request.getIncrementBy();
            }

            pa.setCurrentProgress(newProgress);
            pa.setUpdatedAt(Instant.now());
            finalValue = newProgress;

            if (newProgress >= ach.getTargetValue() && !pa.isUnlocked()) {
                pa.setUnlocked(true);
                pa.setUnlockedAt(Instant.now());

                // Award rewards
                if (ach.getRewardCoins() > 0 || ach.getRewardGems() > 0) {
                    playerService.addCurrency(playerId, ach.getRewardCoins(), ach.getRewardGems());
                    totalCoinsAwarded += ach.getRewardCoins();
                    totalGemsAwarded += ach.getRewardGems();
                }
                if (ach.getRewardXp() > 0) {
                    player.setXp(player.getXp() + ach.getRewardXp());
                    totalXpAwarded += ach.getRewardXp();
                }

                log.info("Achievement UNLOCKED for Player {}: '{}' ({})", player.getUsername(), ach.getTitle(), ach.getAchievementKey());

                double pct = 100.0;
                newlyUnlocked.add(PlayerAchievementResponse.builder()
                        .achievementId(ach.getId())
                        .achievementKey(ach.getAchievementKey())
                        .title(ach.getTitle())
                        .description(ach.getDescription())
                        .category(ach.getCategory().name())
                        .targetMetric(ach.getTargetMetric())
                        .targetValue(ach.getTargetValue())
                        .currentProgress(newProgress)
                        .progressPercentage(pct)
                        .unlocked(true)
                        .unlockedAt(pa.getUnlockedAt())
                        .rewardCoins(ach.getRewardCoins())
                        .rewardXp(ach.getRewardXp())
                        .rewardGems(ach.getRewardGems())
                        .badgeIconUrl(ach.getBadgeIconUrl())
                        .build());
            }

            playerAchievementRepository.save(pa);
        }

        playerRepository.save(player);

        return AchievementUnlockNotification.builder()
                .metricProcessed(metric)
                .newMetricValue(finalValue)
                .newlyUnlockedAchievements(newlyUnlocked)
                .totalCoinsAwarded(totalCoinsAwarded)
                .totalXpAwarded(totalXpAwarded)
                .totalGemsAwarded(totalGemsAwarded)
                .build();
    }

    private AchievementResponse mapToResponse(Achievement a) {
        return AchievementResponse.builder()
                .id(a.getId())
                .achievementKey(a.getAchievementKey())
                .title(a.getTitle())
                .description(a.getDescription())
                .category(a.getCategory())
                .targetMetric(a.getTargetMetric())
                .targetValue(a.getTargetValue())
                .rewardCoins(a.getRewardCoins())
                .rewardXp(a.getRewardXp())
                .rewardGems(a.getRewardGems())
                .badgeIconUrl(a.getBadgeIconUrl())
                .build();
    }
}
