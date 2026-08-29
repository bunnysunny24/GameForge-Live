package com.gameforge.live.liveops;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "live_events", indexes = {
        @Index(name = "idx_event_active_times", columnList = "active, startTime, endTime")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String eventKey;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EventType eventType;

    @Builder.Default
    private double multiplier = 1.0;

    @Column(nullable = false)
    private Instant startTime;

    @Column(nullable = false)
    private Instant endTime;

    @Builder.Default
    private boolean active = true;

    public boolean isCurrentlyLive(Instant now) {
        return active && !now.isBefore(startTime) && !now.isAfter(endTime);
    }
}
