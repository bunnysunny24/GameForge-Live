package com.gameforge.live.leaderboard;

import com.gameforge.live.player.Player;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "leaderboard_entries",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_leaderboard_player", columnNames = {"leaderboard_name", "player_id"})
        },
        indexes = {
                @Index(name = "idx_lb_name_score", columnList = "leaderboard_name, score DESC, updated_at ASC")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "leaderboard_name", nullable = false, length = 50)
    @Builder.Default
    private String leaderboardName = "GLOBAL";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(nullable = false)
    private double score;

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
