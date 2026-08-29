package com.gameforge.live.analytics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameforge.live.analytics.dto.AnalyticsDashboardResponse;
import com.gameforge.live.analytics.dto.PlayerAnalyticsSummary;
import com.gameforge.live.analytics.dto.TrackEventRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private GameEventRepository gameEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void trackEvent_SavesEventSuccessfully() {
        TrackEventRequest request = TrackEventRequest.builder()
                .eventType(GameEventType.GAME_COMPLETED)
                .gameMode("RACING")
                .sessionDurationSeconds(600L)
                .score(12500.0)
                .build();

        GameEvent saved = GameEvent.builder()
                .id(1L)
                .playerId(10L)
                .eventType(GameEventType.GAME_COMPLETED)
                .gameMode("RACING")
                .sessionDurationSeconds(600L)
                .score(12500.0)
                .build();

        when(gameEventRepository.save(any(GameEvent.class))).thenReturn(saved);

        GameEvent result = analyticsService.trackEvent(10L, request);

        assertNotNull(result);
        assertEquals(GameEventType.GAME_COMPLETED, result.getEventType());
        assertEquals("RACING", result.getGameMode());
        assertEquals(600L, result.getSessionDurationSeconds());
    }

    @Test
    void getPlayerAnalyticsSummary_CalculatesWinRateAndPlaytime() {
        GameEvent login = GameEvent.builder().playerId(1L).eventType(GameEventType.PLAYER_LOGIN).sessionDurationSeconds(0L).timestamp(Instant.now()).build();
        GameEvent win1 = GameEvent.builder().playerId(1L).eventType(GameEventType.GAME_COMPLETED).sessionDurationSeconds(500L).timestamp(Instant.now()).build();
        GameEvent win2 = GameEvent.builder().playerId(1L).eventType(GameEventType.GAME_COMPLETED).sessionDurationSeconds(700L).timestamp(Instant.now()).build();
        GameEvent fail1 = GameEvent.builder().playerId(1L).eventType(GameEventType.LEVEL_FAILED).sessionDurationSeconds(200L).timestamp(Instant.now()).build();

        when(gameEventRepository.findByPlayerIdOrderByTimestampDesc(1L)).thenReturn(List.of(login, win1, win2, fail1));

        PlayerAnalyticsSummary summary = analyticsService.getPlayerAnalyticsSummary(1L);

        assertNotNull(summary);
        assertEquals(1L, summary.getPlayerId());
        assertEquals(1L, summary.getTotalSessions());
        assertEquals(1400L, summary.getTotalPlaytimeSeconds());
        assertEquals(2L, summary.getGamesCompleted());
        assertEquals(1L, summary.getGamesFailed());
        assertEquals(66.7, summary.getWinRatePercentage());
    }
}
