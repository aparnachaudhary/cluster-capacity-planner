package io.github.aparnachaudhary.capacityplanner.listener;

import io.github.aparnachaudhary.capacityplanner.domain.ClusterBalance;
import lombok.extern.slf4j.Slf4j;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.optaplanner.core.api.solver.event.SolverEventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ClusterBalanceSolverEventListener implements SolverEventListener<ClusterBalance> {

    @Override
    public void bestSolutionChanged(BestSolutionChangedEvent<ClusterBalance> event) {
        if (event.getNewBestSolution().getScore().isFeasible()) {
            event.getNewBestSolution().getClusterProcesses().forEach(cloudProcess -> log.info(cloudProcess.toString()));
        }
    }
}
