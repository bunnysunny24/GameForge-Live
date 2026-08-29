package com.gameforge.live.leaderboard;

import com.gameforge.live.leaderboard.dto.LeaderboardEntryDto;
import com.gameforge.live.leaderboard.dto.ScoreSubmissionRequest;
import com.gameforge.live.player.Player;
import com.gameforge.live.player.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    @Mock
    private LeaderboardRepository leaderboardRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private LeaderboardServiceImpl leaderboardService;

    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        player1 = Player.builder().id(1L).username("PlayerOne").level(5).build();
        player2 = Player.builder().id(2L).username("PlayerTwo").level(8).build();
    }

    @Test
    void submitScore_NewScoreRecorded() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));
        when(leaderboardRepository.findByLeaderboardNameAndPlayerId("GLOBAL", 1L)).thenReturn(Optional.empty());

        LeaderboardEntry savedEntry = LeaderboardEntry.builder()
                .id(10L)
                .leaderboardName("GLOBAL")
                .player(player1)
                .score(12500.0)
                .updatedAt(Instant.now())
                .build();

        when(leaderboardRepository.save(any(LeaderboardEntry.class))).thenReturn(savedEntry);
        when(leaderboardRepository.calculatePlayerRank(eq("GLOBAL"), eq(12500.0), any())).thenReturn(1L);

        ScoreSubmissionRequest request = ScoreSubmissionRequest.builder()
                .leaderboardName("GLOBAL")
                .score(12500.0)
                .build();

        LeaderboardEntryDto result = leaderboardService.submitScore(1L, request);

        assertNotNull(result);
        assertEquals(1L, result.getRank());
        assertEquals("PlayerOne", result.getUsername());
        assertEquals(12500.0, result.getScore());
    }

    @Test
    void getTopScores_ReturnsOrderedList() {
        LeaderboardEntry entry1 = LeaderboardEntry.builder().player(player2).score(15000.0).updatedAt(Instant.now()).build();
        LeaderboardEntry entry2 = LeaderboardEntry.builder().player(player1).score(12000.0).updatedAt(Instant.now()).build();

        when(leaderboardRepository.findTopScores(eq("GLOBAL"), any(Pageable.class))).thenReturn(List.of(entry1, entry2));

        List<LeaderboardEntryDto> top = leaderboardService.getTopScores("GLOBAL", 10);

        assertEquals(2, top.size());
        assertEquals(1L, top.get(0).getRank());
        assertEquals("PlayerTwo", top.get(0).getUsername());
        assertEquals(15000.0, top.get(0).getScore());
        assertEquals(2L, top.get(1).getRank());
        assertEquals("PlayerOne", top.get(1).getUsername());
    }
}
