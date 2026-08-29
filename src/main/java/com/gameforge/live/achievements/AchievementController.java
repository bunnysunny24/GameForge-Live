package com.gameforge.live.achievements;

import com.gameforge.live.achievements.dto.AchievementResponse;
import com.gameforge.live.achievements.dto.AchievementUnlockNotification;
import com.gameforge.live.achievements.dto.GameplayEventRequest;
import com.gameforge.live.achievements.dto.PlayerAchievementResponse;
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

import java.util.List;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
@Tag(name = "Achievement Engine", description = "Rule-based achievement tracking, telemetry event triggers, and unlock rewards")
public class AchievementController {

    private final AchievementEngine achievementEngine;

    @GetMapping
    @Operation(summary = "List all game achievements", description = "Fetches catalog of all available achievements in the game.")
    public ResponseEntity<ApiResponse<List<AchievementResponse>>> getAllAchievements() {
        return ResponseEntity.ok(ApiResponse.ok(achievementEngine.getAllAchievements()));
    }

    @GetMapping("/player/{playerId}")
    @Operation(summary = "Get player achievement progress", description = "Fetches unlock status and current progress percentage for a player.")
    public ResponseEntity<ApiResponse<List<PlayerAchievementResponse>>> getPlayerAchievements(@PathVariable Long playerId) {
        return ResponseEntity.ok(ApiResponse.ok(achievementEngine.getPlayerAchievements(playerId)));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get current authenticated player achievements", description = "Fetches achievement progression for logged in player.")
    public ResponseEntity<ApiResponse<List<PlayerAchievementResponse>>> getMyAchievements(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok(achievementEngine.getPlayerAchievements(userDetails.getId())));
    }

    @PostMapping("/process-event")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Trigger gameplay event evaluation", description = "Submits a player telemetry event (e.g. races won, matches played) to evaluate achievement rules and award unlocks.")
    public ResponseEntity<ApiResponse<AchievementUnlockNotification>> processEvent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody GameplayEventRequest request
    ) {
        AchievementUnlockNotification result = achievementEngine.processGameplayEvent(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Gameplay event evaluated", result));
    }
}
