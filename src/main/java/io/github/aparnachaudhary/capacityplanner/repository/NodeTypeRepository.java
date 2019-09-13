package io.github.aparnachaudhary.capacityplanner.repository;

import io.github.aparnachaudhary.capacityplanner.domain.NodeType;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface NodeTypeRepository extends PagingAndSortingRepository<NodeType, Long> {
}
