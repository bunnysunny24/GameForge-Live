package com.gameforge.live.experiments.dto;

import com.gameforge.live.experiments.ExperimentVariant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateExperimentRequest {

    @NotBlank(message = "Experiment key is required")
    private String experimentKey;

    private String description;

    @NotEmpty(message = "At least one variant must be provided")
    private List<ExperimentVariant> variants;

    @Builder.Default
    private boolean active = true;
}
