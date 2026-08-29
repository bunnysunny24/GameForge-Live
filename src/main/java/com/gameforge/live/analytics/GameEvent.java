package com.gameforge.live.analytics;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "game_events", indexes = {
        @Index(name = "idx_event_type_time", columnList = "event_type, timestamp"),
        @Index(name = "idx_player_events", columnList = "player_id, timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private GameEventType eventType;

    @Column(name = "game_mode", length = 50)
    private String gameMode; // e.g. "RACING", "BATTLE_ROYALE", "CAMPAIGN"

    @Column(name = "session_duration_seconds")
    @Builder.Default
    private Long sessionDurationSeconds = 0L;

    @Column(name = "score")
    private Double score;

    @Column(name = "metadata_json", length = 1000)
    private String metadataJson;

    @Builder.Default
    @Column(nullable = false)
    private Instant timestamp = Instant.now();
}
