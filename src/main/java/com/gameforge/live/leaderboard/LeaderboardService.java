package com.gameforge.live.leaderboard;

import com.gameforge.live.leaderboard.dto.LeaderboardEntryDto;
import com.gameforge.live.leaderboard.dto.PlayerRankDto;
import com.gameforge.live.leaderboard.dto.ScoreSubmissionRequest;

import java.util.List;

public interface LeaderboardService {
    LeaderboardEntryDto submitScore(Long playerId, ScoreSubmissionRequest request);
    List<LeaderboardEntryDto> getTopScores(String leaderboardName, int limit);
    PlayerRankDto getPlayerRank(String leaderboardName, Long playerId);
}
