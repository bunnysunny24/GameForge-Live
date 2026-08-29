package com.gameforge.live.liveops.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveMultipliersDto {
    private double xpMultiplier;
    private double coinMultiplier;
    private List<LiveEventResponse> activeEvents;
}
