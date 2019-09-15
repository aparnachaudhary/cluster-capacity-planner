package io.github.aparnachaudhary.capacityplanner.repository;

import io.github.aparnachaudhary.capacityplanner.domain.ClusterProcess;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ClusterProcessRepository extends PagingAndSortingRepository<ClusterProcess, Long> {
}
