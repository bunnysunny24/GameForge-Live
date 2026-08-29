package com.gameforge.live.achievements;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerAchievementRepository extends JpaRepository<PlayerAchievement, Long> {

    Optional<PlayerAchievement> findByPlayerIdAndAchievementId(Long playerId, Long achievementId);

    @Query("SELECT pa FROM PlayerAchievement pa JOIN FETCH pa.achievement WHERE pa.player.id = :playerId")
    List<PlayerAchievement> findAllByPlayerIdWithAchievement(@Param("playerId") Long playerId);

    @Query("SELECT pa FROM PlayerAchievement pa JOIN FETCH pa.achievement WHERE pa.player.id = :playerId AND pa.unlocked = true")
    List<PlayerAchievement> findUnlockedByPlayerId(@Param("playerId") Long playerId);
}
