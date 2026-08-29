package com.gameforge.live.leaderboard;

import com.gameforge.live.common.ResourceNotFoundException;
import com.gameforge.live.leaderboard.dto.LeaderboardEntryDto;
import com.gameforge.live.leaderboard.dto.PlayerRankDto;
import com.gameforge.live.leaderboard.dto.ScoreSubmissionRequest;
import com.gameforge.live.player.Player;
import com.gameforge.live.player.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaderboardServiceImpl implements LeaderboardService {

    private final LeaderboardRepository leaderboardRepository;
    private final PlayerRepository playerRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${gameforge.leaderboard.use-redis:false}")
    private boolean useRedis;

    @Value("${gameforge.leaderboard.default-limit:10}")
    private int defaultLimit;

    private static final String REDIS_LEADERBOARD_PREFIX = "leaderboard:";

    @Override
    @Transactional
    public LeaderboardEntryDto submitScore(Long playerId, ScoreSubmissionRequest request) {
        String lbName = (request.getLeaderboardName() != null && !request.getLeaderboardName().isBlank())
                ? request.getLeaderboardName().toUpperCase() : "GLOBAL";

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with ID: " + playerId));

        Optional<LeaderboardEntry> existing = leaderboardRepository.findByLeaderboardNameAndPlayerId(lbName, playerId);
        LeaderboardEntry entry;
        double newScore = request.getScore();

        if (existing.isPresent()) {
            entry = existing.get();
            if (newScore > entry.getScore()) {
                entry.setScore(newScore);
                entry.setUpdatedAt(Instant.now());
            }
        } else {
            entry = LeaderboardEntry.builder()
                    .leaderboardName(lbName)
                    .player(player)
                    .score(newScore)
                    .updatedAt(Instant.now())
                    .build();
        }

        LeaderboardEntry saved = leaderboardRepository.save(entry);

        // Update Redis ZSet if enabled
        if (useRedis) {
            try {
                String redisKey = REDIS_LEADERBOARD_PREFIX + lbName;
                redisTemplate.opsForZSet().add(redisKey, playerId.toString(), saved.getScore());
            } catch (Exception e) {
                log.warn("Failed to write score to Redis for player {}: {}", playerId, e.getMessage());
            }
        }

        long rank = calculateRank(lbName, saved.getScore(), saved.getUpdatedAt(), playerId);

        return LeaderboardEntryDto.builder()
                .rank(rank)
                .playerId(player.getId())
                .username(player.getUsername())
                .avatarUrl(player.getAvatarUrl())
                .level(player.getLevel())
                .score(saved.getScore())
                .recordedAt(saved.getUpdatedAt())
                .build();
    }

    @Override
    public List<LeaderboardEntryDto> getTopScores(String leaderboardName, int limit) {
        String lbName = (leaderboardName != null && !leaderboardName.isBlank()) ? leaderboardName.toUpperCase() : "GLOBAL";
        int fetchLimit = limit > 0 ? limit : defaultLimit;

        if (useRedis) {
            try {
                String redisKey = REDIS_LEADERBOARD_PREFIX + lbName;
                Set<ZSetOperations.TypedTuple<Object>> redisResults =
                        redisTemplate.opsForZSet().reverseRangeWithScores(redisKey, 0, fetchLimit - 1);

                if (redisResults != null && !redisResults.isEmpty()) {
                    List<LeaderboardEntryDto> list = new ArrayList<>();
                    long currentRank = 1;
                    for (ZSetOperations.TypedTuple<Object> tuple : redisResults) {
                        if (tuple.getValue() != null && tuple.getScore() != null) {
                            Long pId = Long.parseLong(tuple.getValue().toString());
                            Player p = playerRepository.findById(pId).orElse(null);
                            if (p != null) {
                                list.add(LeaderboardEntryDto.builder()
                                        .rank(currentRank++)
                                        .playerId(p.getId())
                                        .username(p.getUsername())
                                        .avatarUrl(p.getAvatarUrl())
                                        .level(p.getLevel())
                                        .score(tuple.getScore())
                                        .recordedAt(Instant.now())
                                        .build());
                            }
                        }
                    }
                    if (!list.isEmpty()) {
                        return list;
                    }
                }
            } catch (Exception e) {
                log.warn("Redis top scores query failed, falling back to JPA: {}", e.getMessage());
            }
        }

        // JPA Fallback
        List<LeaderboardEntry> entries = leaderboardRepository.findTopScores(lbName, PageRequest.of(0, fetchLimit));
        long rank = 1;
        List<LeaderboardEntryDto> result = new ArrayList<>();
        for (LeaderboardEntry e : entries) {
            result.add(LeaderboardEntryDto.builder()
                    .rank(rank++)
                    .playerId(e.getPlayer().getId())
                    .username(e.getPlayer().getUsername())
                    .avatarUrl(e.getPlayer().getAvatarUrl())
                    .level(e.getPlayer().getLevel())
                    .score(e.getScore())
                    .recordedAt(e.getUpdatedAt())
                    .build());
        }
        return result;
    }

    @Override
    public PlayerRankDto getPlayerRank(String leaderboardName, Long playerId) {
        String lbName = (leaderboardName != null && !leaderboardName.isBlank()) ? leaderboardName.toUpperCase() : "GLOBAL";

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with ID: " + playerId));

        LeaderboardEntry entry = leaderboardRepository.findByLeaderboardNameAndPlayerId(lbName, playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player has no score record in leaderboard: " + lbName));

        long totalCount = leaderboardRepository.countEntries(lbName);
        long rank = calculateRank(lbName, entry.getScore(), entry.getUpdatedAt(), playerId);

        double percentile = totalCount > 0 ? ((double) rank / (double) totalCount) * 100.0 : 100.0;

        // Fetch surrounding players
        List<LeaderboardEntryDto> topScores = getTopScores(lbName, 20);
        int entryIndex = -1;
        for (int i = 0; i < topScores.size(); i++) {
            if (topScores.get(i).getPlayerId().equals(playerId)) {
                entryIndex = i;
                break;
            }
        }

        List<LeaderboardEntryDto> surrounding = new ArrayList<>();
        if (entryIndex != -1) {
            int start = Math.max(0, entryIndex - 2);
            int end = Math.min(topScores.size(), entryIndex + 3);
            surrounding = topScores.subList(start, end);
        }

        return PlayerRankDto.builder()
                .leaderboardName(lbName)
                .playerId(player.getId())
                .username(player.getUsername())
                .rank(rank)
                .score(entry.getScore())
                .totalParticipants(totalCount)
                .topPercentile(Math.round(percentile * 10.0) / 10.0)
                .surroundingPlayers(surrounding)
                .build();
    }

    private long calculateRank(String lbName, double score, Instant updatedAt, Long playerId) {
        if (useRedis) {
            try {
                String redisKey = REDIS_LEADERBOARD_PREFIX + lbName;
                Long redisRank = redisTemplate.opsForZSet().reverseRank(redisKey, playerId.toString());
                if (redisRank != null) {
                    return redisRank + 1;
                }
            } catch (Exception e) {
                log.warn("Redis rank calculation failed, falling back to JPA: {}", e.getMessage());
            }
        }
        return leaderboardRepository.calculatePlayerRank(lbName, score, updatedAt);
    }
}
