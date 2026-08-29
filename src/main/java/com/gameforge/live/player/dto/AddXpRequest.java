package com.gameforge.live.player.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddXpRequest {
    @Min(value = 1, message = "XP amount must be at least 1")
    private long baseAmount;

    private String source; // e.g. "RACE_WIN", "DAILY_QUEST", "MATCH_COMPLETED"
}
