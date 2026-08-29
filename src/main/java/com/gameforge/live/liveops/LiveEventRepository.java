package com.gameforge.live.liveops;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface LiveEventRepository extends JpaRepository<LiveEvent, Long> {

    Optional<LiveEvent> findByEventKey(String eventKey);

    @Query("SELECT e FROM LiveEvent e WHERE e.active = true AND e.startTime <= :now AND e.endTime >= :now")
    List<LiveEvent> findCurrentlyActiveEvents(@Param("now") Instant now);

    @Query("SELECT e FROM LiveEvent e WHERE e.active = true AND e.endTime >= :now ORDER BY e.startTime ASC")
    List<LiveEvent> findUpcomingAndActiveEvents(@Param("now") Instant now);
}
