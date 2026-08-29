package com.gameforge.live.liveops;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LiveOpsServiceTest {

    @Mock
    private LiveEventRepository liveEventRepository;

    @InjectMocks
    private LiveOpsService liveOpsService;

    @Test
    void getActiveXpMultiplier_ReturnsHighestActiveMultiplier() {
        Instant now = Instant.now();
        LiveEvent event1 = LiveEvent.builder()
                .eventType(EventType.DOUBLE_XP)
                .multiplier(2.0)
                .startTime(now.minus(1, ChronoUnit.HOURS))
                .endTime(now.plus(1, ChronoUnit.HOURS))
                .active(true)
                .build();

        LiveEvent event2 = LiveEvent.builder()
                .eventType(EventType.TRIPLE_XP)
                .multiplier(3.0)
                .startTime(now.minus(1, ChronoUnit.HOURS))
                .endTime(now.plus(1, ChronoUnit.HOURS))
                .active(true)
                .build();

        when(liveEventRepository.findCurrentlyActiveEvents(any(Instant.class))).thenReturn(List.of(event1, event2));

        double multiplier = liveOpsService.getActiveXpMultiplier();

        assertEquals(3.0, multiplier);
    }

    @Test
    void getActiveXpMultiplier_NoEvents_ReturnsDefaultOne() {
        when(liveEventRepository.findCurrentlyActiveEvents(any(Instant.class))).thenReturn(List.of());

        double multiplier = liveOpsService.getActiveXpMultiplier();

        assertEquals(1.0, multiplier);
    }
}
