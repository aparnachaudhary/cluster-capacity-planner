package io.github.aparnachaudhary.capacityplanner.domain;

import lombok.Getter;
import lombok.Setter;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;

import java.util.List;

@PlanningSolution
public class CloudBalance extends AbstractEntity {

    @Getter
    @Setter
    @PlanningScore
    protected HardMediumSoftScore score;

    @Getter
    @Setter
    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "computerProvider")
    private List<CloudComputer> cloudComputers;

    @Getter
    @Setter
    @PlanningEntityCollectionProperty
    private List<CloudProcess> cloudProcesses;

    public CloudBalance(Long id, List<CloudComputer> cloudComputers, List<CloudProcess> cloudProcesses) {
        super(id);
        this.cloudComputers = cloudComputers;
        this.cloudProcesses = cloudProcesses;
    }

    public CloudBalance() {
    }
}
