package com.gameforge.live.experiments;

import com.gameforge.live.common.ApiResponse;
import com.gameforge.live.experiments.dto.CreateExperimentRequest;
import com.gameforge.live.experiments.dto.ExperimentAssignmentResponse;
import com.gameforge.live.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/experiments")
@RequiredArgsConstructor
@Tag(name = "A/B Testing & Live Experimentation", description = "Deterministic variant allocation, reward tuning, and live game experimentation")
public class ExperimentController {

    private final ExperimentService experimentService;

    @GetMapping("/player/{playerId}/variants")
    @Operation(summary = "Get all assigned experiment variants for player", description = "Determines variant bucket (e.g. Control vs Treatment) and configuration payload for a player.")
    public ResponseEntity<ApiResponse<List<ExperimentAssignmentResponse>>> getPlayerVariants(@PathVariable Long playerId) {
        return ResponseEntity.ok(ApiResponse.ok(experimentService.getAllPlayerAssignments(playerId)));
    }

    @GetMapping("/me/variants")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get assigned experiment variants for authenticated player", description = "Fetches assigned test variants for the current session.")
    public ResponseEntity<ApiResponse<List<ExperimentAssignmentResponse>>> getMyVariants(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok(experimentService.getAllPlayerAssignments(userDetails.getId())));
    }

    @GetMapping("/{experimentKey}/player/{playerId}")
    @Operation(summary = "Get specific experiment assignment for player", description = "Evaluates a single experiment.")
    public ResponseEntity<ApiResponse<ExperimentAssignmentResponse>> getAssignment(
            @PathVariable String experimentKey,
            @PathVariable Long playerId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(experimentService.getPlayerAssignment(experimentKey, playerId)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Create an A/B Experiment (Admin)", description = "Registers a new live experiment with variant traffic split and configuration JSON.")
    public ResponseEntity<ApiResponse<Experiment>> createExperiment(@Valid @RequestBody CreateExperimentRequest request) {
        Experiment experiment = experimentService.createExperiment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Experiment created successfully", experiment));
    }
}
