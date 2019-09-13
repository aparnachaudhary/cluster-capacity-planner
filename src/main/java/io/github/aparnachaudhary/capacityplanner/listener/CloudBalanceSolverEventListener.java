package io.github.aparnachaudhary.capacityplanner.listener;

import io.github.aparnachaudhary.capacityplanner.domain.CloudBalance;
import lombok.extern.slf4j.Slf4j;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.optaplanner.core.api.solver.event.SolverEventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CloudBalanceSolverEventListener implements SolverEventListener<CloudBalance> {

    @Override
    public void bestSolutionChanged(BestSolutionChangedEvent<CloudBalance> event) {
        if (event.getNewBestSolution().getScore().isFeasible()) {
            event.getNewBestSolution().getCloudProcesses().forEach(cloudProcess -> log.info(cloudProcess.toString()));
        }
    }
}
