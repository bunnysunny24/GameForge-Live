package com.gameforge.live.liveops;

import com.gameforge.live.common.BadRequestException;
import com.gameforge.live.common.ResourceNotFoundException;
import com.gameforge.live.liveops.dto.ActiveMultipliersDto;
import com.gameforge.live.liveops.dto.CreateLiveEventRequest;
import com.gameforge.live.liveops.dto.LiveEventResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiveOpsService {

    private final LiveEventRepository liveEventRepository;

    public double getActiveXpMultiplier() {
        Instant now = Instant.now();
        List<LiveEvent> activeEvents = liveEventRepository.findCurrentlyActiveEvents(now);
        return activeEvents.stream()
                .filter(e -> e.getEventType() == EventType.DOUBLE_XP || e.getEventType() == EventType.TRIPLE_XP)
                .mapToDouble(LiveEvent::getMultiplier)
                .max()
                .orElse(1.0);
    }

    public double getActiveCoinMultiplier() {
        Instant now = Instant.now();
        List<LiveEvent> activeEvents = liveEventRepository.findCurrentlyActiveEvents(now);
        return activeEvents.stream()
                .filter(e -> e.getEventType() == EventType.DOUBLE_COINS)
                .mapToDouble(LiveEvent::getMultiplier)
                .max()
                .orElse(1.0);
    }

    public ActiveMultipliersDto getActiveMultipliers() {
        Instant now = Instant.now();
        List<LiveEvent> activeEvents = liveEventRepository.findCurrentlyActiveEvents(now);
        List<LiveEventResponse> eventResponses = activeEvents.stream()
                .map(e -> mapToResponse(e, now))
                .collect(Collectors.toList());

        return ActiveMultipliersDto.builder()
                .xpMultiplier(getActiveXpMultiplier())
                .coinMultiplier(getActiveCoinMultiplier())
                .activeEvents(eventResponses)
                .build();
    }

    public List<LiveEventResponse> getAllEvents() {
        Instant now = Instant.now();
        return liveEventRepository.findAll().stream()
                .map(e -> mapToResponse(e, now))
                .collect(Collectors.toList());
    }

    public List<LiveEventResponse> getUpcomingAndActiveEvents() {
        Instant now = Instant.now();
        return liveEventRepository.findUpcomingAndActiveEvents(now).stream()
                .map(e -> mapToResponse(e, now))
                .collect(Collectors.toList());
    }

    @Transactional
    public LiveEventResponse createEvent(CreateLiveEventRequest request) {
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BadRequestException("Event end time cannot be before start time");
        }
        if (liveEventRepository.findByEventKey(request.getEventKey()).isPresent()) {
            throw new BadRequestException("Live Event with key '" + request.getEventKey() + "' already exists");
        }

        LiveEvent event = LiveEvent.builder()
                .eventKey(request.getEventKey())
                .title(request.getTitle())
                .description(request.getDescription())
                .eventType(request.getEventType())
                .multiplier(request.getMultiplier())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .active(request.isActive())
                .build();

        LiveEvent saved = liveEventRepository.save(event);
        log.info("Created Live Event: ID={}, Key={}, Type={}", saved.getId(), saved.getEventKey(), saved.getEventType());
        return mapToResponse(saved, Instant.now());
    }

    @Transactional
    public LiveEventResponse toggleEventActive(Long id, boolean active) {
        LiveEvent event = liveEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Live Event not found with ID: " + id));
        event.setActive(active);
        LiveEvent saved = liveEventRepository.save(event);
        log.info("Toggled Live Event ID={} active status to {}", id, active);
        return mapToResponse(saved, Instant.now());
    }

    private LiveEventResponse mapToResponse(LiveEvent event, Instant now) {
        boolean currentlyLive = event.isCurrentlyLive(now);
        long remaining = 0;
        if (currentlyLive) {
            remaining = Duration.between(now, event.getEndTime()).getSeconds();
        }

        return LiveEventResponse.builder()
                .id(event.getId())
                .eventKey(event.getEventKey())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventType(event.getEventType())
                .multiplier(event.getMultiplier())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .active(event.isActive())
                .currentlyLive(currentlyLive)
                .secondsRemaining(Math.max(0, remaining))
                .build();
    }
}
