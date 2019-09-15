package io.github.aparnachaudhary.capacityplanner.solver;

import io.github.aparnachaudhary.capacityplanner.domain.ClusterProcess;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;

import static org.optaplanner.core.api.score.stream.ConstraintCollectors.count;
import static org.optaplanner.core.api.score.stream.ConstraintCollectors.sum;

public class CloudBalancingConstraintProvider implements ConstraintProvider {

    // WARNING: The ConstraintStreams/ConstraintProvider API is TECH PREVIEW.
    // It works but it has many API gaps.
    // Therefore, it is not rich enough yet to handle complex constraints.

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                requiredCpuPowerTotal(constraintFactory),
                requiredMemoryTotal(constraintFactory),
                requiredDiskUsageTotal(constraintFactory),
                computerCost(constraintFactory)
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    private Constraint requiredCpuPowerTotal(ConstraintFactory constraintFactory) {
        return constraintFactory.from(ClusterProcess.class)
                .groupBy(ClusterProcess::getClusterNode, sum(ClusterProcess::getCpu))
                .filter((cloudComputer, cpuRequired) -> cpuRequired > cloudComputer.getCpu())
                .penalize("requiredCpuPowerTotal",
                        HardMediumSoftScore.ONE_HARD,
                        (cloudComputer, cpuRequired) -> cpuRequired - cloudComputer.getCpu());
    }

    private Constraint requiredMemoryTotal(ConstraintFactory constraintFactory) {
        return constraintFactory.from(ClusterProcess.class)
                .groupBy(ClusterProcess::getClusterNode, sum(ClusterProcess::getMemory))
                .filter((cloudComputer, memoryRequired) -> memoryRequired > cloudComputer.getMemory())
                .penalize("requiredMemoryTotal",
                        HardMediumSoftScore.ONE_HARD,
                        (cloudComputer, memoryRequired) -> memoryRequired - cloudComputer.getMemory());
    }

    private Constraint requiredDiskUsageTotal(ConstraintFactory constraintFactory) {
        return constraintFactory.from(ClusterProcess.class)
                .groupBy(ClusterProcess::getClusterNode, sum(ClusterProcess::getDisk))
                .filter((cloudComputer, diskRequired) -> diskRequired > cloudComputer.getDisk())
                .penalize("requiredDiskUsageTotal",
                        HardMediumSoftScore.ONE_MEDIUM,
                        (cloudComputer, diskRequired) -> diskRequired - cloudComputer.getDisk());
    }

    // ************************************************************************
    // Medium Constraints
    // ************************************************************************

    private Constraint notAssigned(ConstraintFactory constraintFactory) {
        return constraintFactory.from(ClusterProcess.class)
                .groupBy(ClusterProcess::getClusterNode, sum(ClusterProcess::getDisk))
                .filter((cloudComputer, diskRequired) -> diskRequired > cloudComputer.getDisk())
                .penalize("notAssigned",
                        HardMediumSoftScore.ONE_MEDIUM,
                        (cloudComputer, diskRequired) -> diskRequired - cloudComputer.getDisk());
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    private Constraint computerCost(ConstraintFactory constraintFactory) {
        return constraintFactory.from(ClusterProcess.class)
                // TODO Simplify by using:
                // .groupBy(ClusterProcess::getComputer)
                // .penalize(ClusterNode::getCost);
                .groupBy(ClusterProcess::getClusterNode, count())
                .penalize("computerCost",
                        HardMediumSoftScore.ONE_SOFT,
                        (cloudComputer, count) -> cloudComputer.getCost());
    }
}
