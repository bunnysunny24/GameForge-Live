package com.gameforge.live.leaderboard;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaderboardRepository extends JpaRepository<LeaderboardEntry, Long> {

    Optional<LeaderboardEntry> findByLeaderboardNameAndPlayerId(String leaderboardName, Long playerId);

    @Query("SELECT e FROM LeaderboardEntry e JOIN FETCH e.player WHERE e.leaderboardName = :name ORDER BY e.score DESC, e.updatedAt ASC")
    List<LeaderboardEntry> findTopScores(@Param("name") String leaderboardName, Pageable pageable);

    @Query("SELECT COUNT(e) + 1 FROM LeaderboardEntry e WHERE e.leaderboardName = :name AND (e.score > :score OR (e.score = :score AND e.updatedAt < :updatedAt))")
    long calculatePlayerRank(@Param("name") String leaderboardName, @Param("score") double score, @Param("updatedAt") java.time.Instant updatedAt);

    @Query("SELECT COUNT(e) FROM LeaderboardEntry e WHERE e.leaderboardName = :name")
    long countEntries(@Param("name") String leaderboardName);
}
