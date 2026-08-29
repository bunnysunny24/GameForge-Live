package com.gameforge.live.featureflags;

import com.gameforge.live.common.ApiResponse;
import com.gameforge.live.featureflags.dto.CreateFeatureFlagRequest;
import com.gameforge.live.featureflags.dto.FeatureFlagEvaluationResponse;
import com.gameforge.live.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/features")
@RequiredArgsConstructor
@Tag(name = "Feature Flags & Controlled Rollouts", description = "Dynamic feature toggling and deterministic percentage rollouts for live service games")
public class FeatureFlagController {

    private final FeatureFlagService featureFlagService;

    @GetMapping("/evaluate/player/{playerId}")
    @Operation(summary = "Evaluate all feature flags for a player", description = "Calculates active feature flags and rollout assignment for the target player ID.")
    public ResponseEntity<ApiResponse<FeatureFlagEvaluationResponse>> evaluatePlayerFlags(@PathVariable Long playerId) {
        return ResponseEntity.ok(ApiResponse.ok(featureFlagService.evaluateAllFlagsForPlayer(playerId)));
    }

    @GetMapping("/evaluate/me")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Evaluate feature flags for authenticated player", description = "Calculates active feature flags for currently logged-in player.")
    public ResponseEntity<ApiResponse<FeatureFlagEvaluationResponse>> evaluateMyFlags(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok(featureFlagService.evaluateAllFlagsForPlayer(userDetails.getId())));
    }

    @GetMapping("/check/{flagKey}")
    @Operation(summary = "Check single feature flag for a player", description = "Returns boolean flag state.")
    public ResponseEntity<ApiResponse<Boolean>> checkFlag(
            @PathVariable String flagKey,
            @RequestParam Long playerId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(featureFlagService.isFeatureEnabledForPlayer(flagKey, playerId)));
    }

    @GetMapping
    @Operation(summary = "List all feature flags", description = "Returns all configured feature flags.")
    public ResponseEntity<ApiResponse<List<FeatureFlag>>> getAllFlags() {
        return ResponseEntity.ok(ApiResponse.ok(featureFlagService.getAllFlags()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Create a feature flag (Admin)", description = "Configures a new feature flag with rollout percentage.")
    public ResponseEntity<ApiResponse<FeatureFlag>> createFlag(@Valid @RequestBody CreateFeatureFlagRequest request) {
        FeatureFlag flag = featureFlagService.createFlag(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Feature flag created successfully", flag));
    }

    @PutMapping("/{id}/rollout")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Update rollout percentage (Admin)", description = "Adjusts rollout percentage (0-100%) for gradual feature releases.")
    public ResponseEntity<ApiResponse<FeatureFlag>> updateRollout(
            @PathVariable Long id,
            @RequestParam int percentage
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Rollout percentage updated", featureFlagService.updateRollout(id, percentage)));
    }

    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Toggle feature flag (Admin)", description = "Enables or disables feature globally.")
    public ResponseEntity<ApiResponse<FeatureFlag>> toggleFlag(
            @PathVariable Long id,
            @RequestParam boolean enabled
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Feature flag status updated", featureFlagService.toggleFlag(id, enabled)));
    }
}
