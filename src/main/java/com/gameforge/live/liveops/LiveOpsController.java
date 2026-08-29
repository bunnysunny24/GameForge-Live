package com.gameforge.live.liveops;

import com.gameforge.live.common.ApiResponse;
import com.gameforge.live.liveops.dto.ActiveMultipliersDto;
import com.gameforge.live.liveops.dto.CreateLiveEventRequest;
import com.gameforge.live.liveops.dto.LiveEventResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/liveops")
@RequiredArgsConstructor
@Tag(name = "LiveOps & Live Events", description = "Dynamic game events scheduling, active XP/coin multipliers, and live operations management")
public class LiveOpsController {

    private final LiveOpsService liveOpsService;

    @GetMapping("/events/active")
    @Operation(summary = "Get currently active live events & multipliers", description = "Returns active events and calculated real-time XP and Coin multipliers for game client HUD.")
    public ResponseEntity<ApiResponse<ActiveMultipliersDto>> getActiveMultipliers() {
        return ResponseEntity.ok(ApiResponse.ok(liveOpsService.getActiveMultipliers()));
    }

    @GetMapping("/events")
    @Operation(summary = "List upcoming and active live events", description = "Returns scheduled and ongoing live operations events.")
    public ResponseEntity<ApiResponse<List<LiveEventResponse>>> getAllEvents() {
        return ResponseEntity.ok(ApiResponse.ok(liveOpsService.getUpcomingAndActiveEvents()));
    }

    @PostMapping("/events")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Schedule a new Live Event (Admin)", description = "Creates and schedules a new LiveOps event with custom multipliers and active time window.")
    public ResponseEntity<ApiResponse<LiveEventResponse>> createEvent(@Valid @RequestBody CreateLiveEventRequest request) {
        LiveEventResponse response = liveOpsService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Live event scheduled successfully", response));
    }

    @PutMapping("/events/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Toggle event status (Admin)", description = "Emergency activation/deactivation of a live event.")
    public ResponseEntity<ApiResponse<LiveEventResponse>> toggleEvent(@PathVariable Long id, @RequestParam boolean active) {
        return ResponseEntity.ok(ApiResponse.ok("Live event updated", liveOpsService.toggleEventActive(id, active)));
    }
}
