package com.gameforge.live.analytics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameforge.live.analytics.dto.AnalyticsDashboardResponse;
import com.gameforge.live.analytics.dto.PlayerAnalyticsSummary;
import com.gameforge.live.analytics.dto.TrackEventRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final GameEventRepository gameEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public GameEvent trackEvent(Long playerId, TrackEventRequest request) {
        String metadataJson = null;
        if (request.getMetadata() != null && !request.getMetadata().isEmpty()) {
            try {
                metadataJson = objectMapper.writeValueAsString(request.getMetadata());
            } catch (Exception e) {
                log.warn("Failed to serialize analytics metadata: {}", e.getMessage());
            }
        }

        GameEvent event = GameEvent.builder()
                .playerId(playerId)
                .eventType(request.getEventType())
                .gameMode(request.getGameMode())
                .sessionDurationSeconds(request.getSessionDurationSeconds() != null ? request.getSessionDurationSeconds() : 0L)
                .score(request.getScore())
                .metadataJson(metadataJson)
                .timestamp(Instant.now())
                .build();

        GameEvent saved = gameEventRepository.save(event);
        log.debug("Ingested Game Event: Player={}, Type={}, Mode={}", playerId, request.getEventType(), request.getGameMode());
        return saved;
    }

    public AnalyticsDashboardResponse getDashboardMetrics() {
        long totalEvents = gameEventRepository.count();
        long gamesPlayed = gameEventRepository.countByEventType(GameEventType.GAME_COMPLETED) +
                           gameEventRepository.countByEventType(GameEventType.GAME_STARTED);

        Instant twentyFourHoursAgo = Instant.now().minus(24, ChronoUnit.HOURS);
        long dau = gameEventRepository.countActivePlayersSince(twentyFourHoursAgo);

        Double avgDuration = gameEventRepository.calculateAverageSessionDuration();
        double avgMinutes = avgDuration != null ? Math.round((avgDuration / 60.0) * 10.0) / 10.0 : 0.0;

        List<Object[]> popularModes = gameEventRepository.findPopularGameModes();
        String mostPopular = "N/A";
        Map<String, Long> modeDistribution = new LinkedHashMap<>();
        if (popularModes != null && !popularModes.isEmpty()) {
            mostPopular = (String) popularModes.get(0)[0];
            for (Object[] row : popularModes) {
                modeDistribution.put((String) row[0], ((Number) row[1]).longValue());
            }
        }

        Map<String, Long> eventTypeCounts = new LinkedHashMap<>();
        for (GameEventType type : GameEventType.values()) {
            eventTypeCounts.put(type.name(), gameEventRepository.countByEventType(type));
        }

        long eventParticipants = gameEventRepository.countByEventType(GameEventType.EVENT_PARTICIPATED);
        double participationRate = dau > 0 ? Math.round(((double) eventParticipants / (double) dau) * 100.0 * 10.0) / 10.0 : 0.0;

        return AnalyticsDashboardResponse.builder()
                .totalEventsTracked(totalEvents)
                .totalGamesPlayed(gamesPlayed)
                .dailyActiveUsers(dau)
                .averageSessionDurationMinutes(avgMinutes)
                .mostPopularGameMode(mostPopular)
                .gameModeDistribution(modeDistribution)
                .eventTypeCounts(eventTypeCounts)
                .liveEventParticipationRate(Math.min(100.0, participationRate))
                .build();
    }

    public PlayerAnalyticsSummary getPlayerAnalyticsSummary(Long playerId) {
        List<GameEvent> events = gameEventRepository.findByPlayerIdOrderByTimestampDesc(playerId);

        long totalSessions = events.stream().filter(e -> e.getEventType() == GameEventType.PLAYER_LOGIN).count();
        long totalPlaytime = events.stream().mapToLong(GameEvent::getSessionDurationSeconds).sum();
        long gamesCompleted = events.stream().filter(e -> e.getEventType() == GameEventType.GAME_COMPLETED).count();
        long gamesFailed = events.stream().filter(e -> e.getEventType() == GameEventType.LEVEL_FAILED).count();

        long totalFinishedGames = gamesCompleted + gamesFailed;
        double winRate = totalFinishedGames > 0 ? ((double) gamesCompleted / (double) totalFinishedGames) * 100.0 : 0.0;

        double avgSessionMinutes = totalSessions > 0 ? ((double) totalPlaytime / totalSessions) / 60.0 : 0.0;

        List<String> recentActivity = events.stream()
                .limit(10)
                .map(e -> String.format("[%s] %s (Mode: %s, Score: %s)",
                        e.getTimestamp(), e.getEventType(), e.getGameMode() != null ? e.getGameMode() : "N/A", e.getScore() != null ? e.getScore().toString() : "N/A"))
                .collect(Collectors.toList());

        return PlayerAnalyticsSummary.builder()
                .playerId(playerId)
                .totalSessions(totalSessions)
                .totalPlaytimeSeconds(totalPlaytime)
                .averageSessionMinutes(Math.round(avgSessionMinutes * 10.0) / 10.0)
                .gamesCompleted(gamesCompleted)
                .gamesFailed(gamesFailed)
                .winRatePercentage(Math.round(winRate * 10.0) / 10.0)
                .recentActivity(recentActivity)
                .build();
    }
}
