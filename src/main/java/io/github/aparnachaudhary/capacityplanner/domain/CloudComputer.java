package io.github.aparnachaudhary.capacityplanner.domain;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.solution.cloner.DeepPlanningClone;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.io.Serializable;

@Entity
@DeepPlanningClone
@Data
@Builder
public class CloudComputer implements Serializable, Comparable<CloudComputer> {

    private static final long serialVersionUID = 2330429295141905631L;

    @PlanningId
    @Id
    protected Long id;

    private int cpuCapacity;
    private int memoryCapacity;
    private int networkCapacity;
    private int cost;
    @OneToOne
    private AvailabilityZone availabilityZone;
    @OneToOne
    private NodeType nodeType;


    public int getDifficultyIndex() {
        return cpuCapacity * memoryCapacity * networkCapacity;
    }

    @Override
    public int compareTo(CloudComputer o) {
        return new CompareToBuilder()
                .append(getClass().getName(), o.getClass().getName())
                .append(id, o.id)
                .toComparison();
    }

    @Override
    public String toString() {

        return "[CloudComputer - " + id +
                " with cpuCapacity:" + cpuCapacity +
                ", memoryCapacity:" + memoryCapacity +
                ", networkCapacity:" + networkCapacity +
                ", availabilityZone:" + availabilityZone +
                ", nodeType:" + nodeType +
                ", cost:" + cost + "]";
    }

}
