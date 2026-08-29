package com.gameforge.live.leaderboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerRankDto {
    private String leaderboardName;
    private Long playerId;
    private String username;
    private long rank;
    private double score;
    private long totalParticipants;
    private double topPercentile;
    private List<LeaderboardEntryDto> surroundingPlayers;
}
