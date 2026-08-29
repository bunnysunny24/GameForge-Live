package com.gameforge.live.featureflags;

import com.gameforge.live.featureflags.dto.FeatureFlagEvaluationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureFlagServiceTest {

    @Mock
    private FeatureFlagRepository featureFlagRepository;

    @InjectMocks
    private FeatureFlagService featureFlagService;

    private FeatureFlag rolloutFlag;
    private FeatureFlag whitelistedFlag;
    private FeatureFlag disabledFlag;

    @BeforeEach
    void setUp() {
        rolloutFlag = FeatureFlag.builder()
                .id(1L)
                .flagKey("NEW_RACING_MODE")
                .enabled(true)
                .rolloutPercentage(50)
                .build();

        whitelistedFlag = FeatureFlag.builder()
                .id(2L)
                .flagKey("VIP_BETA_ACCESS")
                .enabled(true)
                .rolloutPercentage(0)
                .whitelistedPlayerIds("101,102,103")
                .build();

        disabledFlag = FeatureFlag.builder()
                .id(3L)
                .flagKey("DEPRECATED_FEATURE")
                .enabled(false)
                .rolloutPercentage(100)
                .build();
    }

    @Test
    void evaluateSingleFlag_WhitelistedPlayer_AlwaysEnabled() {
        FeatureFlagService.EvaluationResult result = featureFlagService.evaluateSingleFlag(whitelistedFlag, 101L);
        assertTrue(result.isEnabled());
        assertEquals("PLAYER_WHITELISTED", result.getReason());

        FeatureFlagService.EvaluationResult nonWhitelisted = featureFlagService.evaluateSingleFlag(whitelistedFlag, 999L);
        assertFalse(nonWhitelisted.isEnabled());
    }

    @Test
    void evaluateSingleFlag_DisabledGlobally_AlwaysDisabled() {
        FeatureFlagService.EvaluationResult result = featureFlagService.evaluateSingleFlag(disabledFlag, 101L);
        assertFalse(result.isEnabled());
        assertEquals("FLAG_DISABLED_GLOBALLY", result.getReason());
    }

    @Test
    void computePlayerBucket_IsDeterministic() {
        int bucket1 = FeatureFlagService.computePlayerBucket("NEW_RACING_MODE", 42L);
        int bucket2 = FeatureFlagService.computePlayerBucket("NEW_RACING_MODE", 42L);
        assertEquals(bucket1, bucket2);
        assertTrue(bucket1 >= 0 && bucket1 < 100);
    }

    @Test
    void evaluateAllFlagsForPlayer_ReturnsMap() {
        when(featureFlagRepository.findAll()).thenReturn(List.of(rolloutFlag, whitelistedFlag, disabledFlag));

        FeatureFlagEvaluationResponse response = featureFlagService.evaluateAllFlagsForPlayer(101L);

        assertNotNull(response);
        assertEquals(101L, response.getPlayerId());
        assertTrue(response.getFeatures().containsKey("NEW_RACING_MODE"));
        assertTrue(response.getFeatures().get("VIP_BETA_ACCESS"));
        assertFalse(response.getFeatures().get("DEPRECATED_FEATURE"));
    }
}
