package io.github.aparnachaudhary.capacityplanner.domain;

import lombok.Builder;
import lombok.Data;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.solution.cloner.DeepPlanningClone;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.List;

@Entity
@DeepPlanningClone
@Data
@Builder
public class AvailabilityZone {

    @PlanningId
    @Id
    protected Long id;

    private String name;

    @Transient
    private List<ClusterNode> clusterNodes;

    @Override
    public String toString() {
        return "AZ - " + name;
    }

}
