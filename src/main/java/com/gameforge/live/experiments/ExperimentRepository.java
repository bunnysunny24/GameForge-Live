package com.gameforge.live.experiments;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExperimentRepository extends JpaRepository<Experiment, Long> {
    Optional<Experiment> findByExperimentKey(String experimentKey);
    List<Experiment> findByActiveTrue();
    boolean existsByExperimentKey(String experimentKey);
}
