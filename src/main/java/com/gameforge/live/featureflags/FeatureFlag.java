package com.gameforge.live.featureflags;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "feature_flags", indexes = {
        @Index(name = "idx_flag_key", columnList = "flag_key", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flag_key", nullable = false, unique = true, length = 100)
    private String flagKey; // e.g. "NEW_RACING_MODE", "DOUBLE_REWARDS_VIP", "BETA_MATCHMAKER"

    @Column(length = 500)
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;

    @Builder.Default
    @Column(name = "rollout_percentage", nullable = false)
    private int rolloutPercentage = 0; // 0 to 100%

    @Column(name = "whitelisted_player_ids", length = 1000)
    private String whitelistedPlayerIds; // Comma separated IDs, e.g. "1,2,105"

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
