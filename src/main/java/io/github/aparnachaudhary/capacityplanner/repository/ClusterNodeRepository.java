package io.github.aparnachaudhary.capacityplanner.repository;

import io.github.aparnachaudhary.capacityplanner.domain.ClusterNode;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ClusterNodeRepository extends PagingAndSortingRepository<ClusterNode, Long> {
}
