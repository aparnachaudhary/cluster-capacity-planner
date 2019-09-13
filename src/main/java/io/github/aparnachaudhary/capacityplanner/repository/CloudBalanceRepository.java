package io.github.aparnachaudhary.capacityplanner.repository;

import io.github.aparnachaudhary.capacityplanner.domain.CloudBalance;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CloudBalanceRepository extends PagingAndSortingRepository<CloudBalance, Long> {
}
