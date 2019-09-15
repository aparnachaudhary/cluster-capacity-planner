package io.github.aparnachaudhary.capacityplanner.solver;

import io.github.aparnachaudhary.capacityplanner.domain.ClusterBalance;
import io.github.aparnachaudhary.capacityplanner.domain.ClusterNode;
import io.github.aparnachaudhary.capacityplanner.domain.ClusterProcess;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;

import java.util.*;

public class CloudBalancingMapBasedEasyScoreCalculator implements EasyScoreCalculator<ClusterBalance> {

    @Override
    public HardMediumSoftScore calculateScore(ClusterBalance clusterBalance) {

        int clusterNodesSize = clusterBalance.getClusterNodes().size();
        Map<ClusterNode, Integer> cpuCapacityUsageMap = new HashMap<>(clusterNodesSize);
        Map<ClusterNode, Integer> memoryCapacityMap = new HashMap<>(clusterNodesSize);
        Map<ClusterNode, Integer> diskCapacityUsageMap = new HashMap<>(clusterNodesSize);

        for (ClusterNode clusterNode : clusterBalance.getClusterNodes()) {
            cpuCapacityUsageMap.put(clusterNode, 0);
            memoryCapacityMap.put(clusterNode, 0);
            diskCapacityUsageMap.put(clusterNode, 0);
        }
        Set<ClusterNode> usedClusterNodes = new HashSet<>(clusterNodesSize);

        visitProcessList(cpuCapacityUsageMap, memoryCapacityMap, diskCapacityUsageMap,
                usedClusterNodes, clusterBalance.getClusterProcesses());

        int hardScore = sumHardScore(cpuCapacityUsageMap, memoryCapacityMap, diskCapacityUsageMap);
        int mediumScore = sumMediumScore(clusterBalance.getClusterProcesses());
        int softScore = sumSoftScore(usedClusterNodes);

        return HardMediumSoftScore.of(hardScore, mediumScore, softScore);
    }

    private void visitProcessList(Map<ClusterNode, Integer> cpuPowerUsageMap,
                                  Map<ClusterNode, Integer> memoryUsageMap, Map<ClusterNode, Integer> diskUsageMap,
                                  Set<ClusterNode> usedClusterNodes, List<ClusterProcess> processList) {

        // We loop through the processList only once for performance
        for (ClusterProcess process : processList) {
            ClusterNode clusterNode = process.getClusterNode();
            if (clusterNode != null) {
                int cpuPowerUsage = cpuPowerUsageMap.get(clusterNode) + process.getCpu();
                cpuPowerUsageMap.put(clusterNode, cpuPowerUsage);
                int memoryUsage = memoryUsageMap.get(clusterNode) + process.getMemory();
                memoryUsageMap.put(clusterNode, memoryUsage);
                int diskUsage = diskUsageMap.get(clusterNode) + process.getDisk();
                diskUsageMap.put(clusterNode, diskUsage);
                usedClusterNodes.add(clusterNode);
            }
        }
    }

    private int sumHardScore(Map<ClusterNode, Integer> cpuPowerUsageMap, Map<ClusterNode, Integer> memoryUsageMap,
                             Map<ClusterNode, Integer> diskUsageMap) {
        int hardScore = 0;
        for (Map.Entry<ClusterNode, Integer> usageEntry : cpuPowerUsageMap.entrySet()) {
            ClusterNode clusterNode = usageEntry.getKey();
            int cpuPowerAvailable = clusterNode.getCpu() - usageEntry.getValue();
            if (cpuPowerAvailable < 0) {
                hardScore += cpuPowerAvailable;
            }
        }
        for (Map.Entry<ClusterNode, Integer> usageEntry : memoryUsageMap.entrySet()) {
            ClusterNode clusterNode = usageEntry.getKey();
            int memoryAvailable = clusterNode.getMemory() - usageEntry.getValue();
            if (memoryAvailable < 0) {
                hardScore += memoryAvailable;
            }
        }
        for (Map.Entry<ClusterNode, Integer> usageEntry : diskUsageMap.entrySet()) {
            ClusterNode clusterNode = usageEntry.getKey();
            int diskAvailable = clusterNode.getDisk() - usageEntry.getValue();
            if (diskAvailable < 0) {
                hardScore += diskAvailable;
            }
        }
        return hardScore;
    }

    private int sumMediumScore(List<ClusterProcess> processSet) {
        int mediumScore = 0;
        for (ClusterProcess clusterProcess : processSet) {
            if (clusterProcess.getClusterNode() == null) {
                mediumScore -= clusterProcess.getDifficultyIndex();
            }
        }
        return mediumScore;
    }

    private int sumSoftScore(Set<ClusterNode> usedClusterNodes) {
        int softScore = 0;
        for (ClusterNode usedClusterNode : usedClusterNodes) {
            softScore -= usedClusterNode.getCost();
        }
        return softScore;
    }
}
