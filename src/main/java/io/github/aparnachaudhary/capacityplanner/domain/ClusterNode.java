package io.github.aparnachaudhary.capacityplanner.domain;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.solution.cloner.DeepPlanningClone;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@DeepPlanningClone
@Data
@Builder
public class ClusterNode implements Serializable, Comparable<ClusterNode> {

    private static final long serialVersionUID = 2330429295141905631L;

    @PlanningId
    @Id
    protected Long id;
    private String name;

    private int cpuCapacity;
    private int memoryCapacity;
    private int diskCapacity;
    private int cost;
    @ManyToOne
    private AvailabilityZone availabilityZone;
    @ManyToOne
    private ClusterNodeType clusterNodeType;

    public int getDifficultyIndex() {
        return cpuCapacity * memoryCapacity * diskCapacity;
    }

    @Override
    public int compareTo(ClusterNode o) {
        return new CompareToBuilder()
                .append(getClass().getName(), o.getClass().getName())
                .append(id, o.id)
                .toComparison();
    }

    @Override
    public String toString() {

        return "[ClusterNode - " + id +
                " with name:" + name +
                ", CPU:" + cpuCapacity +
                ", MEM:" + memoryCapacity +
                ", DISK:" + diskCapacity +
                ", AZ:" + availabilityZone.getName() +
                ", clusterNodeType:" + clusterNodeType.getName() +
                ", cost:" + cost + "]";
    }


}
