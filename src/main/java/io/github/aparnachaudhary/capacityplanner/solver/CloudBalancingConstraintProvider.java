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
                .groupBy(ClusterProcess::getClusterNode, sum(ClusterProcess::getCpuRequired))
                .filter((cloudComputer, cpuRequired) -> cpuRequired > cloudComputer.getCpuCapacity())
                .penalize("requiredCpuPowerTotal",
                        HardMediumSoftScore.ONE_HARD,
                        (cloudComputer, cpuRequired) -> cpuRequired - cloudComputer.getCpuCapacity());
    }

    private Constraint requiredMemoryTotal(ConstraintFactory constraintFactory) {
        return constraintFactory.from(ClusterProcess.class)
                .groupBy(ClusterProcess::getClusterNode, sum(ClusterProcess::getMemoryRequired))
                .filter((cloudComputer, memoryRequired) -> memoryRequired > cloudComputer.getMemoryCapacity())
                .penalize("requiredMemoryTotal",
                        HardMediumSoftScore.ONE_HARD,
                        (cloudComputer, memoryRequired) -> memoryRequired - cloudComputer.getMemoryCapacity());
    }

    private Constraint requiredDiskUsageTotal(ConstraintFactory constraintFactory) {
        return constraintFactory.from(ClusterProcess.class)
                .groupBy(ClusterProcess::getClusterNode, sum(ClusterProcess::getDiskRequired))
                .filter((cloudComputer, diskRequired) -> diskRequired > cloudComputer.getDiskCapacity())
                .penalize("requiredDiskUsageTotal",
                        HardMediumSoftScore.ONE_MEDIUM,
                        (cloudComputer, diskRequired) -> diskRequired - cloudComputer.getDiskCapacity());
    }

    // ************************************************************************
    // Medium Constraints
    // ************************************************************************

    private Constraint notAssigned(ConstraintFactory constraintFactory) {
        return constraintFactory.from(ClusterProcess.class)
                .groupBy(ClusterProcess::getClusterNode, sum(ClusterProcess::getDiskRequired))
                .filter((cloudComputer, diskRequired) -> diskRequired > cloudComputer.getDiskCapacity())
                .penalize("notAssigned",
                        HardMediumSoftScore.ONE_MEDIUM,
                        (cloudComputer, diskRequired) -> diskRequired - cloudComputer.getDiskCapacity());
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
