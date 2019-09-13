package io.github.aparnachaudhary.capacityplanner.domain;

import io.github.aparnachaudhary.capacityplanner.extension.ComputerStrengthComparator;
import io.github.aparnachaudhary.capacityplanner.extension.ProcessDifficultyComparator;
import lombok.Getter;
import lombok.Setter;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.solution.cloner.DeepPlanningClone;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@PlanningEntity(difficultyComparatorClass = ProcessDifficultyComparator.class)
@Entity
@DeepPlanningClone
public class CloudProcess extends AbstractEntity {

    @Setter
    @Getter
    private int cpuRequired;
    @Setter
    @Getter
    private int memoryRequired;
    @Setter
    @Getter
    private int networkRequired;

    @Setter
    @Getter
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

    public CloudProcess(Long id, int cpuRequired, int memoryRequired, int networkRequired) {
        super(id);
        this.cpuRequired = cpuRequired;
        this.memoryRequired = memoryRequired;
        this.networkRequired = networkRequired;
    }

    public CloudProcess(Long id, int cpuRequired, int memoryRequired, int networkRequired, CloudComputer cloudComputer) {
        super(id);
        this.cpuRequired = cpuRequired;
        this.memoryRequired = memoryRequired;
        this.networkRequired = networkRequired;
        this.cloudComputer = cloudComputer;
    }

    public CloudProcess(Long id) {
        super(id);
    }

    public CloudProcess() {
    }
}
