package com.gameforge.live.featureflags.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFeatureFlagRequest {

    @NotBlank(message = "Flag key is required")
    private String flagKey;

    private String description;

    @Builder.Default
    private boolean enabled = true;

    @Min(value = 0, message = "Rollout percentage cannot be less than 0")
    @Max(value = 100, message = "Rollout percentage cannot exceed 100")
    @Builder.Default
    private int rolloutPercentage = 0;

    private String whitelistedPlayerIds;
}
