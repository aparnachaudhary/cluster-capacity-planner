package io.github.aparnachaudhary.capacityplanner.repository;

import io.github.aparnachaudhary.capacityplanner.domain.ClusterNodeType;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ClusterNodeTypeRepository extends PagingAndSortingRepository<ClusterNodeType, Long> {
}
