package io.github.aparnachaudhary.capacityplanner.solver;

import io.github.aparnachaudhary.capacityplanner.domain.ClusterBalance;
import io.github.aparnachaudhary.capacityplanner.domain.ClusterNode;
import io.github.aparnachaudhary.capacityplanner.domain.ClusterProcess;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;

public class CloudBalancingEasyScoreCalculator implements EasyScoreCalculator<ClusterBalance> {

    /**
     * A very simple implementation. The double loop can easily be removed by using Maps as shown in
     * {@link CloudBalancingMapBasedEasyScoreCalculator#calculateScore(ClusterBalance)}.
     */
    @Override
    public HardSoftScore calculateScore(ClusterBalance clusterBalance) {

        int hardScore = 0;
        int softScore = 0;

        for (ClusterNode computer : clusterBalance.getClusterNodes()) {
            int cpuCapacityUsage = 0;
            int memoryUsage = 0;
            int diskUsage = 0;
            boolean used = false;

            // Calculate usage
            for (ClusterProcess process : clusterBalance.getClusterProcesses()) {
                if (computer.equals(process.getClusterNode())) {
                    cpuCapacityUsage += process.getCpuRequired();
                    memoryUsage += process.getMemoryRequired();
                    diskUsage += process.getDiskRequired();
                    used = true;
                }
            }

            // Hard constraints
            int cpuPowerAvailable = computer.getCpuCapacity() - cpuCapacityUsage;
            if (cpuPowerAvailable < 0) {
                hardScore += cpuPowerAvailable;
            }
            int memoryAvailable = computer.getMemoryCapacity() - memoryUsage;
            if (memoryAvailable < 0) {
                hardScore += memoryAvailable;
            }
            int diskAvailable = computer.getDiskCapacity() - diskUsage;
            if (diskAvailable < 0) {
                hardScore += diskAvailable;
            }

            // Soft constraints
            if (used) {
                softScore -= computer.getCost();
            }
        }
        return HardSoftScore.of(hardScore, softScore);
    }
}
