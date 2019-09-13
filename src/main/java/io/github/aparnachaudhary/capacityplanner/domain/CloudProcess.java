package io.github.aparnachaudhary.capacityplanner.domain;

import io.github.aparnachaudhary.capacityplanner.extension.ComputerStrengthComparator;
import io.github.aparnachaudhary.capacityplanner.extension.ProcessDifficultyComparator;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.solution.cloner.DeepPlanningClone;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@PlanningEntity(difficultyComparatorClass = ProcessDifficultyComparator.class)
@Entity
@DeepPlanningClone
@Data
@Builder
public class CloudProcess implements Serializable, Comparable<CloudProcess> {

    @PlanningId
    @Id
    protected Long id;

    private int cpuRequired;
    private int memoryRequired;
    private int networkRequired;


    @PlanningVariable(valueRangeProviderRefs = "computerProvider", strengthComparatorClass = ComputerStrengthComparator.class, nullable = true)
    @ManyToOne
    private CloudComputer cloudComputer;

    public int getDifficultyIndex() {
        return cpuRequired * memoryRequired * networkRequired;
    }

    @Override
    public String toString() {
        return "CloudProcess - " + id +
                " with cpuRequired:" + cpuRequired +
                ", memoryRequired:" + memoryRequired+
                ", networkRequired:" + networkRequired +
                "; Assigned to CloudComputer: " + cloudComputer;
    }

    @Override
    public int compareTo(CloudProcess o) {
        return new CompareToBuilder().append(getClass().getName(), o.getClass().getName())
                .append(id, o.id).toComparison();
    }

}
