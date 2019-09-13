package io.github.aparnachaudhary.capacityplanner.listener;

import io.github.aparnachaudhary.capacityplanner.domain.CloudBalance;
import io.github.aparnachaudhary.capacityplanner.repository.CloudBalanceRepository;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.optaplanner.core.api.solver.event.SolverEventListener;
import org.springframework.stereotype.Component;

@Component
public class CloudBalanceSolverEventListener implements SolverEventListener<CloudBalance> {

    private CloudBalanceRepository cloudBalanceRepository;

    public CloudBalanceSolverEventListener(CloudBalanceRepository cloudBalanceRepository) {
        this.cloudBalanceRepository = cloudBalanceRepository;
    }

    @Override
    public void bestSolutionChanged(BestSolutionChangedEvent<CloudBalance> event) {
        if (event.getNewBestSolution().getScore().isFeasible()) {
            //cloudBalanceRepository.save(event.getNewBestSolution());
            //System.out.println(event.getNewBestSolution());
            event.getNewBestSolution().getCloudProcesses().forEach(System.out::println);
        }
    }
}
