package com.gameforge.live.analytics;

import com.gameforge.live.analytics.dto.AnalyticsDashboardResponse;
import com.gameforge.live.analytics.dto.PlayerAnalyticsSummary;
import com.gameforge.live.analytics.dto.TrackEventRequest;
import com.gameforge.live.common.ApiResponse;
import com.gameforge.live.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Game Analytics & Telemetry", description = "High-throughput event tracking, gameplay telemetry ingestion, and live operations dashboard metrics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping("/events")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Track game telemetry event", description = "Ingests game client events such as GAME_STARTED, GAME_COMPLETED, LEVEL_FAILED, etc.")
    public ResponseEntity<ApiResponse<GameEvent>> trackEvent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TrackEventRequest request
    ) {
        GameEvent saved = analyticsService.trackEvent(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Event ingested successfully", saved));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get LiveOps platform dashboard metrics", description = "Returns aggregated game statistics: total events, DAU, average session duration, most played modes, and event engagement.")
    public ResponseEntity<ApiResponse<AnalyticsDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getDashboardMetrics()));
    }

    @GetMapping("/player/{playerId}")
    @Operation(summary = "Get Player analytics summary", description = "Returns sessions, playtime, win rate, and recent activity logs for a player.")
    public ResponseEntity<ApiResponse<PlayerAnalyticsSummary>> getPlayerAnalytics(@PathVariable Long playerId) {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getPlayerAnalyticsSummary(playerId)));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get current authenticated player analytics summary", description = "Returns personal sessions, playtime, and win rate.")
    public ResponseEntity<ApiResponse<PlayerAnalyticsSummary>> getMyAnalytics(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getPlayerAnalyticsSummary(userDetails.getId())));
    }
}
