package com.gameforge.live.leaderboard;

import com.gameforge.live.common.ApiResponse;
import com.gameforge.live.leaderboard.dto.LeaderboardEntryDto;
import com.gameforge.live.leaderboard.dto.PlayerRankDto;
import com.gameforge.live.leaderboard.dto.ScoreSubmissionRequest;
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
@RequestMapping("/api/leaderboards")
@RequiredArgsConstructor
@Tag(name = "Leaderboards & Ranking", description = "High-performance ranking APIs powered by Redis Sorted Sets and PostgreSQL")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @PostMapping("/scores")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Submit a player score", description = "Records a new score, updates high-scores, and ranks player across global/seasonal leaderboards.")
    public ResponseEntity<ApiResponse<LeaderboardEntryDto>> submitScore(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ScoreSubmissionRequest request
    ) {
        LeaderboardEntryDto result = leaderboardService.submitScore(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Score submitted successfully", result));
    }

    @GetMapping("/global")
    @Operation(summary = "Get Top Global Leaderboard", description = "Fetches Top N players globally sorted by highest score.")
    public ResponseEntity<ApiResponse<List<LeaderboardEntryDto>>> getGlobalTop(
            @RequestParam(defaultValue = "GLOBAL") String leaderboardName,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.ok(leaderboardService.getTopScores(leaderboardName, limit)));
    }

    @GetMapping("/player/{playerId}/rank")
    @Operation(summary = "Get Player's Rank & Surrounding Competition", description = "Fetches exact rank, top percentile, and nearby competitive players.")
    public ResponseEntity<ApiResponse<PlayerRankDto>> getPlayerRank(
            @PathVariable Long playerId,
            @RequestParam(defaultValue = "GLOBAL") String leaderboardName
    ) {
        return ResponseEntity.ok(ApiResponse.ok(leaderboardService.getPlayerRank(leaderboardName, playerId)));
    }
}
