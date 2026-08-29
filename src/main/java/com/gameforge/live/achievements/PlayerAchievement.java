package com.gameforge.live.achievements;

import com.gameforge.live.player.Player;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "player_achievements",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_player_achievement", columnNames = {"player_id", "achievement_id"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;

    @Builder.Default
    @Column(name = "current_progress", nullable = false)
    private long currentProgress = 0L;

    @Builder.Default
    @Column(nullable = false)
    private boolean unlocked = false;

    @Column(name = "unlocked_at")
    private Instant unlockedAt;

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
