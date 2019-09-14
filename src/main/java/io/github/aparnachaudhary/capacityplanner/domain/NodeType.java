package io.github.aparnachaudhary.capacityplanner.domain;

import lombok.Builder;
import lombok.Data;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.solution.cloner.DeepPlanningClone;

import javax.persistence.*;

@Entity
@DeepPlanningClone
@Data
@Builder
public class NodeType {

    @PlanningId
    @Id
    protected Long id;

    private String name;

//    @OneToMany
//    private List<CloudComputer> cloudComputers;

    @Override
    public String toString() {
        return "NodeType - " + id +
                " with name:" + name;
    }

//    public int getCpuCapacity() {
//        return cloudComputers.stream().mapToInt(CloudComputer::getCpuCapacity).sum();
//    }
}
