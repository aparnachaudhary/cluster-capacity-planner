package io.github.aparnachaudhary.capacityplanner.domain;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;

import javax.persistence.Id;
import java.io.Serializable;
import java.util.List;

@PlanningSolution
@Data
@Builder
public class CloudBalance implements Serializable, Comparable<CloudBalance> {

    @PlanningId
    @Id
    protected Long id;

    @PlanningScore
    protected HardMediumSoftScore score;

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "computerProvider")
    private List<CloudComputer> cloudComputers;

    @PlanningEntityCollectionProperty
    private List<CloudProcess> cloudProcesses;
//    private List<NodeType> nodeTypes;
//    private List<AvailabilityZone> availabilityZones;


    @Override
    public int compareTo(CloudBalance o) {
        return new CompareToBuilder().append(getClass().getName(), o.getClass().getName())
                .append(id, o.id).toComparison();
    }

    @Override
    public String toString() {
        return getClass().getName().replaceAll(".*\\.", "") + "-" + id;
    }
}
