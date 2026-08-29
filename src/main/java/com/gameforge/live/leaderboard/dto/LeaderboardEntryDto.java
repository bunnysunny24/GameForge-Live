package com.gameforge.live.leaderboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntryDto {
    private long rank;
    private Long playerId;
    private String username;
    private String avatarUrl;
    private int level;
    private double score;
    private Instant recordedAt;
}
