package com.gameforge.live.experiments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperimentVariant {
    private String name; // e.g. "CONTROL", "TREATMENT_HIGH_REWARD"
    private int weight;  // e.g. 50 (50% traffic)
    private String configJson; // e.g. "{\"rewardCoins\": 200}"
}
