package com.gameforge.live.player.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddXpResponse {
    private Long playerId;
    private long baseAmount;
    private double eventMultiplier;
    private long finalAwardedXp;
    private long totalXp;
    private int oldLevel;
    private int newLevel;
    private boolean leveledUp;
    private long bonusCoinsAwarded;
    private int bonusGemsAwarded;
}
