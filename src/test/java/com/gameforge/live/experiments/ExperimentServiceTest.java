package com.gameforge.live.experiments;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameforge.live.experiments.dto.ExperimentAssignmentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExperimentServiceTest {

    @Mock
    private ExperimentRepository experimentRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ExperimentService experimentService;

    private Experiment rewardExperiment;

    @BeforeEach
    void setUp() {
        String json = "[{\"name\":\"CONTROL\",\"weight\":50,\"configJson\":\"{\\\"coins\\\":100}\"},{\"name\":\"TREATMENT_B\",\"weight\":50,\"configJson\":\"{\\\"coins\\\":200}\"}]";
        rewardExperiment = Experiment.builder()
                .id(1L)
                .experimentKey("DAILY_REWARD_EXP")
                .description("Daily login coin test")
                .variantsJson(json)
                .active(true)
                .build();
    }

    @Test
    void assignVariant_IsDeterministic() {
        List<ExperimentVariant> variants = List.of(
                ExperimentVariant.builder().name("CONTROL").weight(50).build(),
                ExperimentVariant.builder().name("TREATMENT").weight(50).build()
        );

        ExperimentVariant assignment1 = ExperimentService.assignVariant("DAILY_REWARD_EXP", 101L, variants);
        ExperimentVariant assignment2 = ExperimentService.assignVariant("DAILY_REWARD_EXP", 101L, variants);

        assertEquals(assignment1.getName(), assignment2.getName());
    }

    @Test
    void getPlayerAssignment_AssignsVariantAndParsesConfig() {
        when(experimentRepository.findByExperimentKey("DAILY_REWARD_EXP")).thenReturn(Optional.of(rewardExperiment));

        ExperimentAssignmentResponse response = experimentService.getPlayerAssignment("DAILY_REWARD_EXP", 101L);

        assertNotNull(response);
        assertEquals("DAILY_REWARD_EXP", response.getExperimentKey());
        assertTrue(List.of("CONTROL", "TREATMENT_B").contains(response.getAssignedVariant()));
        assertNotNull(response.getParsedConfig());
    }
}
