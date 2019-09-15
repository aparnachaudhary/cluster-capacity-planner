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

        int computerListSize = clusterBalance.getClusterNodes().size();
        Map<ClusterNode, Integer> cpuCapacityUsageMap = new HashMap<>(computerListSize);
        Map<ClusterNode, Integer> memoryCapacityMap = new HashMap<>(computerListSize);
        Map<ClusterNode, Integer> diskCapacityUsageMap = new HashMap<>(computerListSize);

        for (ClusterNode computer : clusterBalance.getClusterNodes()) {
            cpuCapacityUsageMap.put(computer, 0);
            memoryCapacityMap.put(computer, 0);
            diskCapacityUsageMap.put(computer, 0);
        }
        Set<ClusterNode> usedComputerSet = new HashSet<>(computerListSize);

        visitProcessList(cpuCapacityUsageMap, memoryCapacityMap, diskCapacityUsageMap,
                usedComputerSet, clusterBalance.getClusterProcesses());

        int hardScore = sumHardScore(cpuCapacityUsageMap, memoryCapacityMap, diskCapacityUsageMap);
        int mediumScore = sumMediumScore(clusterBalance.getClusterProcesses());
        int softScore = sumSoftScore(usedComputerSet);

        return HardMediumSoftScore.of(hardScore, mediumScore, softScore);
    }

    private void visitProcessList(Map<ClusterNode, Integer> cpuPowerUsageMap,
                                  Map<ClusterNode, Integer> memoryUsageMap, Map<ClusterNode, Integer> diskUsageMap,
                                  Set<ClusterNode> usedComputerSet, List<ClusterProcess> processList) {

        // We loop through the processList only once for performance
        for (ClusterProcess process : processList) {
            ClusterNode computer = process.getClusterNode();
            if (computer != null) {
                int cpuPowerUsage = cpuPowerUsageMap.get(computer) + process.getCpuRequired();
                cpuPowerUsageMap.put(computer, cpuPowerUsage);
                int memoryUsage = memoryUsageMap.get(computer) + process.getMemoryRequired();
                memoryUsageMap.put(computer, memoryUsage);
                int diskUsage = diskUsageMap.get(computer) + process.getDiskRequired();
                diskUsageMap.put(computer, diskUsage);
                usedComputerSet.add(computer);
            }
        }
    }

    private int sumHardScore(Map<ClusterNode, Integer> cpuPowerUsageMap, Map<ClusterNode, Integer> memoryUsageMap,
                             Map<ClusterNode, Integer> diskUsageMap) {
        int hardScore = 0;
        for (Map.Entry<ClusterNode, Integer> usageEntry : cpuPowerUsageMap.entrySet()) {
            ClusterNode computer = usageEntry.getKey();
            int cpuPowerAvailable = computer.getCpuCapacity() - usageEntry.getValue();
            if (cpuPowerAvailable < 0) {
                hardScore += cpuPowerAvailable;
            }
        }
        for (Map.Entry<ClusterNode, Integer> usageEntry : memoryUsageMap.entrySet()) {
            ClusterNode computer = usageEntry.getKey();
            int memoryAvailable = computer.getMemoryCapacity() - usageEntry.getValue();
            if (memoryAvailable < 0) {
                hardScore += memoryAvailable;
            }
        }
        for (Map.Entry<ClusterNode, Integer> usageEntry : diskUsageMap.entrySet()) {
            ClusterNode computer = usageEntry.getKey();
            int diskAvailable = computer.getDiskCapacity() - usageEntry.getValue();
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

    private int sumSoftScore(Set<ClusterNode> usedComputerSet) {
        int softScore = 0;
        for (ClusterNode usedComputer : usedComputerSet) {
            softScore -= usedComputer.getCost();
        }
        return softScore;
    }
}
