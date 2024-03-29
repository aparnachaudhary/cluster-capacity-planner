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

        for (ClusterNode clusterNode : clusterBalance.getClusterNodes()) {
            int cpuCapacityUsage = 0;
            int memoryUsage = 0;
            int diskUsage = 0;
            boolean used = false;

            // Calculate usage
            for (ClusterProcess process : clusterBalance.getClusterProcesses()) {
                if (clusterNode.equals(process.getClusterNode())) {
                    cpuCapacityUsage += process.getCpu();
                    memoryUsage += process.getMemory();
                    diskUsage += process.getDisk();
                    used = true;
                }
            }

            // Hard constraints
            int cpuPowerAvailable = clusterNode.getCpu() - cpuCapacityUsage;
            if (cpuPowerAvailable < 0) {
                hardScore += cpuPowerAvailable;
            }
            int memoryAvailable = clusterNode.getMemory() - memoryUsage;
            if (memoryAvailable < 0) {
                hardScore += memoryAvailable;
            }
            int diskAvailable = clusterNode.getDisk() - diskUsage;
            if (diskAvailable < 0) {
                hardScore += diskAvailable;
            }

            // Soft constraints
            if (used) {
                softScore -= clusterNode.getCost();
            }
        }
        return HardSoftScore.of(hardScore, softScore);
    }
}
