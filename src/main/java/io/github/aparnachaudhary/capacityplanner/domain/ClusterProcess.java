package io.github.aparnachaudhary.capacityplanner.domain;

import io.github.aparnachaudhary.capacityplanner.extension.ClusterNodeStrengthComparator;
import io.github.aparnachaudhary.capacityplanner.extension.ProcessDifficultyComparator;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.solution.cloner.DeepPlanningClone;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.io.Serializable;

@PlanningEntity(difficultyComparatorClass = ProcessDifficultyComparator.class)
@Entity
@DeepPlanningClone
@Data
@Builder
public class ClusterProcess implements Serializable, Comparable<ClusterProcess> {

    private static final long serialVersionUID = -224283897820531278L;

    @PlanningId
    @Id
    protected Long id;
    private String name;

    private int cpu;
    private int memory;
    private int disk;

    @OneToOne
    private AvailabilityZone availabilityZone;

    @OneToOne
    private ClusterNodeType clusterNodeType;

    @PlanningVariable(valueRangeProviderRefs = "clusterNodeProvider", strengthComparatorClass = ClusterNodeStrengthComparator.class, nullable = true)
    @ManyToOne
    private ClusterNode clusterNode;

    public int getDifficultyIndex() {
        return cpu * memory * disk;
    }

    @Override
    public String toString() {

        return "ClusterProcess-" + id +
                " with name:" + name +
                ", cpu:" + cpu +
                ", memory:" + memory +
                ", disk:" + disk +
                ", AZ:" + availabilityZone.getName() +
                ", clusterNodeType:" + clusterNodeType.getName() +
                "; assignedToClusterNode=" + clusterNode;
    }

    public String displayString() {

        return "[ClusterProcess-" + id +
                " with name:" + name +
                ", cpu:" + cpu +
                ", memory:" + memory +
                ", disk:" + disk +
                ", AZ:" + availabilityZone.getName() +
                ", clusterNodeType:" + clusterNodeType.getName() +
                "] assignedToClusterNode=[" + clusterNode.getName() + "]";
    }

    @Override
    public int compareTo(ClusterProcess o) {
        return new CompareToBuilder()
                .append(getClass().getName(), o.getClass().getName())
                .append(id, o.id)
                .toComparison();
    }

}
