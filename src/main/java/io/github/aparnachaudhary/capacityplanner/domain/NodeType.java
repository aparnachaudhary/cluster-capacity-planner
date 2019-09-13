package io.github.aparnachaudhary.capacityplanner.domain;

import lombok.Builder;
import lombok.Data;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.solution.cloner.DeepPlanningClone;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@DeepPlanningClone
@Data
@Builder
public class NodeType {

    @PlanningId
    @Id
    protected Long id;

    private String name;
}
