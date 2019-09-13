package io.github.aparnachaudhary.capacityplanner.repository;

import io.github.aparnachaudhary.capacityplanner.domain.CloudProcess;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CloudProcessRepository extends PagingAndSortingRepository<CloudProcess, Long> {
}
