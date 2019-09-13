package io.github.aparnachaudhary.capacityplanner.repository;

import io.github.aparnachaudhary.capacityplanner.domain.AvailabilityZone;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface AvailabilityZoneRepository extends PagingAndSortingRepository<AvailabilityZone, Long> {
}
