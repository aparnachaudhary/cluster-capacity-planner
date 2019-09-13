package io.github.aparnachaudhary.capacityplanner.repository;

import io.github.aparnachaudhary.capacityplanner.domain.CloudComputer;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CloudComputerRepository extends PagingAndSortingRepository<CloudComputer, Long> {
}
