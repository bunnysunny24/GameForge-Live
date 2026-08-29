package com.gameforge.live.analytics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface GameEventRepository extends JpaRepository<GameEvent, Long> {

    List<GameEvent> findByPlayerIdOrderByTimestampDesc(Long playerId);

    long countByEventType(GameEventType eventType);

    long countByEventTypeAndTimestampAfter(GameEventType eventType, Instant after);

    @Query("SELECT AVG(e.sessionDurationSeconds) FROM GameEvent e WHERE e.sessionDurationSeconds > 0")
    Double calculateAverageSessionDuration();

    @Query("SELECT e.gameMode, COUNT(e) FROM GameEvent e WHERE e.gameMode IS NOT NULL GROUP BY e.gameMode ORDER BY COUNT(e) DESC")
    List<Object[]> findPopularGameModes();

    @Query("SELECT COUNT(DISTINCT e.playerId) FROM GameEvent e WHERE e.timestamp >= :since")
    long countActivePlayersSince(@Param("since") Instant since);
}
