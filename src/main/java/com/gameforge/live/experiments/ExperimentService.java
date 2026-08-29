package com.gameforge.live.experiments;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameforge.live.common.BadRequestException;
import com.gameforge.live.common.ResourceNotFoundException;
import com.gameforge.live.experiments.dto.CreateExperimentRequest;
import com.gameforge.live.experiments.dto.ExperimentAssignmentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.zip.CRC32;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExperimentService {

    private final ExperimentRepository experimentRepository;
    private final ObjectMapper objectMapper;

    public List<Experiment> getAllActiveExperiments() {
        return experimentRepository.findByActiveTrue();
    }

    public ExperimentAssignmentResponse getPlayerAssignment(String experimentKey, Long playerId) {
        Experiment experiment = experimentRepository.findByExperimentKey(experimentKey)
                .orElseThrow(() -> new ResourceNotFoundException("Experiment not found with key: " + experimentKey));

        if (!experiment.isActive()) {
            return ExperimentAssignmentResponse.builder()
                    .playerId(playerId)
                    .experimentKey(experimentKey)
                    .assignedVariant("INACTIVE")
                    .variantConfigJson("{}")
                    .parsedConfig(Collections.emptyMap())
                    .build();
        }

        List<ExperimentVariant> variants = parseVariants(experiment.getVariantsJson());
        if (variants.isEmpty()) {
            throw new BadRequestException("Experiment has no configured variants");
        }

        ExperimentVariant assigned = assignVariant(experimentKey, playerId, variants);
        Map<String, Object> configMap = parseConfig(assigned.getConfigJson());

        return ExperimentAssignmentResponse.builder()
                .playerId(playerId)
                .experimentKey(experimentKey)
                .assignedVariant(assigned.getName())
                .variantConfigJson(assigned.getConfigJson())
                .parsedConfig(configMap)
                .build();
    }

    public List<ExperimentAssignmentResponse> getAllPlayerAssignments(Long playerId) {
        List<Experiment> activeExperiments = experimentRepository.findByActiveTrue();
        return activeExperiments.stream()
                .map(exp -> getPlayerAssignment(exp.getExperimentKey(), playerId))
                .collect(Collectors.toList());
    }

    @Transactional
    public Experiment createExperiment(CreateExperimentRequest request) {
        if (experimentRepository.existsByExperimentKey(request.getExperimentKey())) {
            throw new BadRequestException("Experiment with key '" + request.getExperimentKey() + "' already exists");
        }

        String json;
        try {
            json = objectMapper.writeValueAsString(request.getVariants());
        } catch (Exception e) {
            throw new BadRequestException("Failed to serialize variants: " + e.getMessage());
        }

        Experiment exp = Experiment.builder()
                .experimentKey(request.getExperimentKey().toUpperCase().trim())
                .description(request.getDescription())
                .variantsJson(json)
                .active(request.isActive())
                .updatedAt(Instant.now())
                .build();

        Experiment saved = experimentRepository.save(exp);
        log.info("Created Experiment: Key={}, VariantsCount={}", saved.getExperimentKey(), request.getVariants().size());
        return saved;
    }

    public static ExperimentVariant assignVariant(String experimentKey, Long playerId, List<ExperimentVariant> variants) {
        int totalWeight = variants.stream().mapToInt(ExperimentVariant::getWeight).sum();
        if (totalWeight <= 0) {
            return variants.get(0);
        }

        String input = "exp:" + experimentKey + ":" + playerId;
        CRC32 crc32 = new CRC32();
        crc32.update(input.getBytes(StandardCharsets.UTF_8));
        long hash = crc32.getValue();
        int slot = (int) (hash % totalWeight);

        int cumulative = 0;
        for (ExperimentVariant v : variants) {
            cumulative += v.getWeight();
            if (slot < cumulative) {
                return v;
            }
        }
        return variants.get(variants.size() - 1);
    }

    public List<ExperimentVariant> parseVariants(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<ExperimentVariant>>() {});
        } catch (Exception e) {
            log.error("Failed to parse experiment variants JSON: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private Map<String, Object> parseConfig(String configJson) {
        if (configJson == null || configJson.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(configJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
