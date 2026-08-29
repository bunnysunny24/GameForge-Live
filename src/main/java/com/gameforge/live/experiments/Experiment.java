package com.gameforge.live.experiments;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "experiments", indexes = {
        @Index(name = "idx_exp_key", columnList = "experiment_key", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Experiment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "experiment_key", nullable = false, unique = true, length = 100)
    private String experimentKey;

    @Column(length = 500)
    private String description;

    @Column(name = "variants_json", nullable = false, length = 2000)
    private String variantsJson;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
