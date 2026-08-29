package com.gameforge.live.featureflags;

import com.gameforge.live.common.BadRequestException;
import com.gameforge.live.common.ResourceNotFoundException;
import com.gameforge.live.featureflags.dto.CreateFeatureFlagRequest;
import com.gameforge.live.featureflags.dto.FeatureFlagEvaluationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.zip.CRC32;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureFlagService {

    private final FeatureFlagRepository featureFlagRepository;

    public List<FeatureFlag> getAllFlags() {
        return featureFlagRepository.findAll();
    }

    public FeatureFlag getFlagByKey(String flagKey) {
        return featureFlagRepository.findByFlagKey(flagKey)
                .orElseThrow(() -> new ResourceNotFoundException("Feature Flag not found with key: " + flagKey));
    }

    public FeatureFlagEvaluationResponse evaluateAllFlagsForPlayer(Long playerId) {
        List<FeatureFlag> flags = featureFlagRepository.findAll();
        Map<String, Boolean> featureMap = new HashMap<>();
        Map<String, String> reasons = new HashMap<>();

        for (FeatureFlag flag : flags) {
            EvaluationResult eval = evaluateSingleFlag(flag, playerId);
            featureMap.put(flag.getFlagKey(), eval.isEnabled());
            reasons.put(flag.getFlagKey(), eval.getReason());
        }

        return FeatureFlagEvaluationResponse.builder()
                .playerId(playerId)
                .features(featureMap)
                .evaluationReasons(reasons)
                .build();
    }

    public boolean isFeatureEnabledForPlayer(String flagKey, Long playerId) {
        FeatureFlag flag = getFlagByKey(flagKey);
        return evaluateSingleFlag(flag, playerId).isEnabled();
    }

    public EvaluationResult evaluateSingleFlag(FeatureFlag flag, Long playerId) {
        if (!flag.isEnabled()) {
            return new EvaluationResult(false, "FLAG_DISABLED_GLOBALLY");
        }

        // Check Whitelist
        if (flag.getWhitelistedPlayerIds() != null && !flag.getWhitelistedPlayerIds().isBlank()) {
            Set<String> whitelisted = Arrays.stream(flag.getWhitelistedPlayerIds().split(","))
                    .map(String::trim)
                    .collect(java.util.stream.Collectors.toSet());
            if (whitelisted.contains(String.valueOf(playerId))) {
                return new EvaluationResult(true, "PLAYER_WHITELISTED");
            }
        }

        if (flag.getRolloutPercentage() >= 100) {
            return new EvaluationResult(true, "ROLLOUT_100_PERCENT");
        }
        if (flag.getRolloutPercentage() <= 0) {
            return new EvaluationResult(false, "ROLLOUT_0_PERCENT");
        }

        // Deterministic Hashing Bucket
        int bucket = computePlayerBucket(flag.getFlagKey(), playerId);
        boolean enabled = bucket < flag.getRolloutPercentage();
        String reason = enabled
                ? String.format("ROLLOUT_INCLUDED (Bucket %d < %d%%)", bucket, flag.getRolloutPercentage())
                : String.format("ROLLOUT_EXCLUDED (Bucket %d >= %d%%)", bucket, flag.getRolloutPercentage());

        return new EvaluationResult(enabled, reason);
    }

    public static int computePlayerBucket(String flagKey, Long playerId) {
        String input = flagKey + ":" + playerId;
        CRC32 crc32 = new CRC32();
        crc32.update(input.getBytes(StandardCharsets.UTF_8));
        long hash = crc32.getValue();
        return (int) (hash % 100);
    }

    @Transactional
    public FeatureFlag createFlag(CreateFeatureFlagRequest request) {
        if (featureFlagRepository.existsByFlagKey(request.getFlagKey())) {
            throw new BadRequestException("Feature flag with key '" + request.getFlagKey() + "' already exists");
        }

        FeatureFlag flag = FeatureFlag.builder()
                .flagKey(request.getFlagKey().toUpperCase().trim())
                .description(request.getDescription())
                .enabled(request.isEnabled())
                .rolloutPercentage(request.getRolloutPercentage())
                .whitelistedPlayerIds(request.getWhitelistedPlayerIds())
                .updatedAt(Instant.now())
                .build();

        FeatureFlag saved = featureFlagRepository.save(flag);
        log.info("Created Feature Flag: Key={}, Rollout={}%", saved.getFlagKey(), saved.getRolloutPercentage());
        return saved;
    }

    @Transactional
    public FeatureFlag updateRollout(Long id, int rolloutPercentage) {
        if (rolloutPercentage < 0 || rolloutPercentage > 100) {
            throw new BadRequestException("Rollout percentage must be between 0 and 100");
        }
        FeatureFlag flag = featureFlagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feature Flag not found with ID: " + id));

        flag.setRolloutPercentage(rolloutPercentage);
        flag.setUpdatedAt(Instant.now());
        FeatureFlag saved = featureFlagRepository.save(flag);
        log.info("Updated Feature Flag ID={} rollout to {}%", id, rolloutPercentage);
        return saved;
    }

    @Transactional
    public FeatureFlag toggleFlag(Long id, boolean enabled) {
        FeatureFlag flag = featureFlagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feature Flag not found with ID: " + id));
        flag.setEnabled(enabled);
        flag.setUpdatedAt(Instant.now());
        return featureFlagRepository.save(flag);
    }

    public static class EvaluationResult {
        private final boolean enabled;
        private final String reason;

        public EvaluationResult(boolean enabled, String reason) {
            this.enabled = enabled;
            this.reason = reason;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getReason() {
            return reason;
        }
    }
}
