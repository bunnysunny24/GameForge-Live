package com.gameforge.live.player;

import com.gameforge.live.common.BadRequestException;
import com.gameforge.live.common.ResourceNotFoundException;
import com.gameforge.live.liveops.LiveOpsService;
import com.gameforge.live.player.dto.AddXpRequest;
import com.gameforge.live.player.dto.AddXpResponse;
import com.gameforge.live.player.dto.PlayerResponse;
import com.gameforge.live.player.dto.UpdatePlayerRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final LiveOpsService liveOpsService;

    public PlayerResponse getPlayerResponseById(Long id) {
        Player player = findEntityById(id);
        return mapToResponse(player);
    }

    public PlayerResponse getPlayerResponseByUsername(String username) {
        Player player = playerRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with username: " + username));
        return mapToResponse(player);
    }

    public Player findEntityById(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with ID: " + id));
    }

    @Transactional
    public PlayerResponse updatePlayer(Long id, UpdatePlayerRequest request) {
        Player player = findEntityById(id);

        if (request.getUsername() != null && !request.getUsername().trim().isEmpty() && !request.getUsername().equals(player.getUsername())) {
            if (playerRepository.existsByUsername(request.getUsername())) {
                throw new BadRequestException("Username '" + request.getUsername() + "' is already taken");
            }
            player.setUsername(request.getUsername().trim());
        }

        if (request.getAvatarUrl() != null && !request.getAvatarUrl().trim().isEmpty()) {
            player.setAvatarUrl(request.getAvatarUrl().trim());
        }

        Player saved = playerRepository.save(player);
        log.info("Updated player ID={}", saved.getId());
        return mapToResponse(saved);
    }

    @Transactional
    public AddXpResponse addXp(Long playerId, AddXpRequest request) {
        Player player = findEntityById(playerId);
        double multiplier = liveOpsService.getActiveXpMultiplier();
        long finalXpAwarded = Math.round(request.getBaseAmount() * multiplier);

        int oldLevel = player.getLevel();
        long newTotalXp = player.getXp() + finalXpAwarded;
        player.setXp(newTotalXp);

        // Level calculation
        int calculatedLevel = calculateLevelFromXp(newTotalXp);
        boolean leveledUp = calculatedLevel > oldLevel;
        long bonusCoins = 0;
        int bonusGems = 0;

        if (leveledUp) {
            int levelsGained = calculatedLevel - oldLevel;
            bonusCoins = levelsGained * 500L;
            bonusGems = levelsGained * 5;
            player.setLevel(calculatedLevel);
            player.setCoins(player.getCoins() + bonusCoins);
            player.setGems(player.getGems() + bonusGems);
            log.info("Player ID={} leveled up from {} to {}! Rewarded {} coins, {} gems",
                    playerId, oldLevel, calculatedLevel, bonusCoins, bonusGems);
        }

        playerRepository.save(player);

        return AddXpResponse.builder()
                .playerId(playerId)
                .baseAmount(request.getBaseAmount())
                .eventMultiplier(multiplier)
                .finalAwardedXp(finalXpAwarded)
                .totalXp(newTotalXp)
                .oldLevel(oldLevel)
                .newLevel(player.getLevel())
                .leveledUp(leveledUp)
                .bonusCoinsAwarded(bonusCoins)
                .bonusGemsAwarded(bonusGems)
                .build();
    }

    @Transactional
    public void addCurrency(Long playerId, long coins, int gems) {
        Player player = findEntityById(playerId);
        player.setCoins(player.getCoins() + coins);
        player.setGems(player.getGems() + gems);
        playerRepository.save(player);
        log.info("Added currency to Player ID={}: +{} coins, +{} gems", playerId, coins, gems);
    }

    public static int calculateLevelFromXp(long totalXp) {
        int level = 1;
        long cumulativeXpNeeded = 0;
        while (true) {
            long xpForNext = (long) level * 1000L;
            if (totalXp >= cumulativeXpNeeded + xpForNext) {
                cumulativeXpNeeded += xpForNext;
                level++;
            } else {
                break;
            }
        }
        return level;
    }

    public static long calculateXpToNextLevel(long totalXp, int currentLevel) {
        long cumulativeBefore = 0;
        for (int i = 1; i < currentLevel; i++) {
            cumulativeBefore += (long) i * 1000L;
        }
        long nextLevelThreshold = cumulativeBefore + ((long) currentLevel * 1000L);
        return Math.max(0, nextLevelThreshold - totalXp);
    }

    public PlayerResponse mapToResponse(Player player) {
        return PlayerResponse.builder()
                .id(player.getId())
                .username(player.getUsername())
                .email(player.getEmail())
                .role(player.getRole().name())
                .level(player.getLevel())
                .xp(player.getXp())
                .xpToNextLevel(calculateXpToNextLevel(player.getXp(), player.getLevel()))
                .coins(player.getCoins())
                .gems(player.getGems())
                .avatarUrl(player.getAvatarUrl())
                .createdAt(player.getCreatedAt())
                .lastLoginAt(player.getLastLoginAt())
                .build();
    }
}
