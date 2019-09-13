package io.github.aparnachaudhary.capacityplanner.domain;

import lombok.Getter;
import lombok.Setter;
import org.optaplanner.core.api.domain.solution.cloner.DeepPlanningClone;

import javax.persistence.Entity;

@Entity
@DeepPlanningClone
public class CloudComputer extends AbstractEntity{

    @Setter
    @Getter
    private int cpuCapacity;
    @Setter
    @Getter
    private int memoryCapacity;
    @Setter
    @Getter
    private int networkCapacity;
    @Setter
    @Getter
    private int cost;

    public int getDifficultyIndex() {
        return cpuCapacity * memoryCapacity * networkCapacity;
    }

    @Override
    public String toString() {
        return "CloudComputer - " + id +
                " with cpu:" + cpuCapacity +
                ", memoryCapacity:" + memoryCapacity+
                ", networkCapacity:" + networkCapacity;
    }

    public CloudComputer(Long id, int cpuCapacity, int memoryCapacity, int networkCapacity, int cost) {
        super(id);
        this.cpuCapacity = cpuCapacity;
        this.memoryCapacity = memoryCapacity;
        this.networkCapacity = networkCapacity;
        this.cost = cost;
    }

    public CloudComputer(Long id) {
        super(id);
    }

    public CloudComputer() {
    }
}
