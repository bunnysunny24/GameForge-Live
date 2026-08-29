package com.gameforge.live.player;

import com.gameforge.live.liveops.LiveOpsService;
import com.gameforge.live.player.dto.AddXpRequest;
import com.gameforge.live.player.dto.AddXpResponse;
import com.gameforge.live.player.dto.PlayerResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private LiveOpsService liveOpsService;

    @InjectMocks
    private PlayerService playerService;

    private Player player;

    @BeforeEach
    void setUp() {
        player = Player.builder()
                .id(1L)
                .username("Bhavashesh")
                .email("bhavashesh@gameforge.live")
                .level(1)
                .xp(500L)
                .coins(1000L)
                .gems(50)
                .build();
    }

    @Test
    void addXp_WithDoubleXpEvent_LevelsUp() {
        // Base 500 XP + awarded (600 * 2.0x = 1200 XP) -> Total 1700 XP
        // Level 1 -> 2 requires 1000 XP. 1700 XP reaches Level 2!
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(liveOpsService.getActiveXpMultiplier()).thenReturn(2.0);
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddXpRequest request = AddXpRequest.builder()
                .baseAmount(600L)
                .source("RACE_WIN")
                .build();

        AddXpResponse response = playerService.addXp(1L, request);

        assertNotNull(response);
        assertEquals(2.0, response.getEventMultiplier());
        assertEquals(1200L, response.getFinalAwardedXp());
        assertEquals(1700L, response.getTotalXp());
        assertTrue(response.isLeveledUp());
        assertEquals(2, response.getNewLevel());
        assertEquals(500L, response.getBonusCoinsAwarded());
        assertEquals(5, response.getBonusGemsAwarded());
    }

    @Test
    void calculateLevelFormulas() {
        assertEquals(1, PlayerService.calculateLevelFromXp(0));
        assertEquals(1, PlayerService.calculateLevelFromXp(999));
        assertEquals(2, PlayerService.calculateLevelFromXp(1000));
        assertEquals(2, PlayerService.calculateLevelFromXp(2999));
        assertEquals(3, PlayerService.calculateLevelFromXp(3000));
    }
}
