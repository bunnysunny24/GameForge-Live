package com.gameforge.live.player;

import com.gameforge.live.common.ApiResponse;
import com.gameforge.live.player.dto.AddXpRequest;
import com.gameforge.live.player.dto.AddXpResponse;
import com.gameforge.live.player.dto.PlayerResponse;
import com.gameforge.live.player.dto.UpdatePlayerRequest;
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
@RequestMapping("/api/players")
@RequiredArgsConstructor
@Tag(name = "Player Management", description = "Player profile, leveling progression, XP awards, and wallet management")
public class PlayerController {

    private final PlayerService playerService;

    @GetMapping("/me")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get current authenticated player profile", description = "Returns full player details including progression, XP to next level, and wallet balances.")
    public ResponseEntity<ApiResponse<PlayerResponse>> getMyProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(playerService.getPlayerResponseById(userDetails.getId())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get public player profile by ID", description = "Fetches player progression and stats.")
    public ResponseEntity<ApiResponse<PlayerResponse>> getPlayerById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(playerService.getPlayerResponseById(id)));
    }

    @PutMapping("/me")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Update current player profile", description = "Updates username or avatar URL.")
    public ResponseEntity<ApiResponse<PlayerResponse>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdatePlayerRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully", playerService.updatePlayer(userDetails.getId(), request)));
    }

    @PostMapping("/me/add-xp")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Award XP to player", description = "Applies active LiveOps event multipliers dynamically and calculates level ups.")
    public ResponseEntity<ApiResponse<AddXpResponse>> addXp(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AddXpRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("XP processed successfully", playerService.addXp(userDetails.getId(), request)));
    }
}
