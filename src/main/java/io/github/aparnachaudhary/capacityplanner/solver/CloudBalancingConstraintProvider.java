package io.github.aparnachaudhary.capacityplanner.solver;

import io.github.aparnachaudhary.capacityplanner.domain.CloudProcess;
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
                requiredNetworkBandwidthTotal(constraintFactory),
                computerCost(constraintFactory)
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    private Constraint requiredCpuPowerTotal(ConstraintFactory constraintFactory) {
        return constraintFactory.from(CloudProcess.class)
                .groupBy(CloudProcess::getCloudComputer, sum(CloudProcess::getCpuRequired))
                .filter((cloudComputer, cpuRequired) -> cpuRequired > cloudComputer.getCpuCapacity())
                .penalize("requiredCpuPowerTotal",
                        HardMediumSoftScore.ONE_HARD,
                        (cloudComputer, cpuRequired) -> cpuRequired - cloudComputer.getCpuCapacity());
    }

    private Constraint requiredMemoryTotal(ConstraintFactory constraintFactory) {
        return constraintFactory.from(CloudProcess.class)
                .groupBy(CloudProcess::getCloudComputer, sum(CloudProcess::getMemoryRequired))
                .filter((cloudComputer, memoryRequired) -> memoryRequired > cloudComputer.getMemoryCapacity())
                .penalize("requiredMemoryTotal",
                        HardMediumSoftScore.ONE_HARD,
                        (cloudComputer, memoryRequired) -> memoryRequired - cloudComputer.getMemoryCapacity());
    }

    private Constraint requiredNetworkBandwidthTotal(ConstraintFactory constraintFactory) {
        return constraintFactory.from(CloudProcess.class)
                .groupBy(CloudProcess::getCloudComputer, sum(CloudProcess::getNetworkRequired))
                .filter((cloudComputer, networkRequired) -> networkRequired > cloudComputer.getNetworkCapacity())
                .penalize("requiredNetworkBandwidthTotal",
                        HardMediumSoftScore.ONE_MEDIUM,
                        (cloudComputer, networkRequired) -> networkRequired - cloudComputer.getNetworkCapacity());
    }

    // ************************************************************************
    // Medium Constraints
    // ************************************************************************

    private Constraint notAssigned(ConstraintFactory constraintFactory) {
        return constraintFactory.from(CloudProcess.class)
                .groupBy(CloudProcess::getCloudComputer, sum(CloudProcess::getNetworkRequired))
                .filter((cloudComputer, requiredNetworkBandwidth) -> requiredNetworkBandwidth > cloudComputer.getNetworkCapacity())
                .penalize("notAssigned",
                        HardMediumSoftScore.ONE_MEDIUM,
                        (cloudComputer, requiredNetworkBandwidth) -> requiredNetworkBandwidth - cloudComputer.getNetworkCapacity());
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    private Constraint computerCost(ConstraintFactory constraintFactory) {
        return constraintFactory.from(CloudProcess.class)
                // TODO Simplify by using:
                // .groupBy(CloudProcess::getComputer)
                // .penalize(CloudComputer::getCost);
                .groupBy(CloudProcess::getCloudComputer, count())
                .penalize("computerCost",
                        HardMediumSoftScore.ONE_SOFT,
                        (cloudComputer, count) -> cloudComputer.getCost());
    }
}
