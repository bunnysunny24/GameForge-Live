package com.gameforge.live.leaderboard.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreSubmissionRequest {

    @Builder.Default
    private String leaderboardName = "GLOBAL";

    @NotNull(message = "Score is required")
    private Double score;

    private String gameMode; // e.g. "RACING", "BATTLE_ROYALE", "ARCADE"
}
