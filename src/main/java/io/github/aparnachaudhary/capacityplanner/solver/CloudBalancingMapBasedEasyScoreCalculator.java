package io.github.aparnachaudhary.capacityplanner.solver;

import io.github.aparnachaudhary.capacityplanner.domain.CloudBalance;
import io.github.aparnachaudhary.capacityplanner.domain.CloudComputer;
import io.github.aparnachaudhary.capacityplanner.domain.CloudProcess;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;

import java.util.*;

public class CloudBalancingMapBasedEasyScoreCalculator implements EasyScoreCalculator<CloudBalance> {

    @Override
    public HardMediumSoftScore calculateScore(CloudBalance cloudBalance) {

        int computerListSize = cloudBalance.getCloudComputers().size();
        Map<CloudComputer, Integer> cpuPowerUsageMap = new HashMap<>(computerListSize);
        Map<CloudComputer, Integer> memoryUsageMap = new HashMap<>(computerListSize);
        Map<CloudComputer, Integer> networkBandwidthUsageMap = new HashMap<>(computerListSize);
        for (CloudComputer computer : cloudBalance.getCloudComputers()) {
            cpuPowerUsageMap.put(computer, 0);
            memoryUsageMap.put(computer, 0);
            networkBandwidthUsageMap.put(computer, 0);
        }
        Set<CloudComputer> usedComputerSet = new HashSet<>(computerListSize);

        visitProcessList(cpuPowerUsageMap, memoryUsageMap, networkBandwidthUsageMap,
                usedComputerSet, cloudBalance.getCloudProcesses());

        int hardScore = sumHardScore(cpuPowerUsageMap, memoryUsageMap, networkBandwidthUsageMap);
        int mediumScore = sumMediumScore(cloudBalance.getCloudProcesses());
        int softScore = sumSoftScore(usedComputerSet);

        return HardMediumSoftScore.of(hardScore, mediumScore, softScore);
    }

    private void visitProcessList(Map<CloudComputer, Integer> cpuPowerUsageMap,
                                  Map<CloudComputer, Integer> memoryUsageMap, Map<CloudComputer, Integer> networkBandwidthUsageMap,
                                  Set<CloudComputer> usedComputerSet, List<CloudProcess> processList) {

        // We loop through the processList only once for performance
        for (CloudProcess process : processList) {
            CloudComputer computer = process.getCloudComputer();
            if (computer != null) {
                int cpuPowerUsage = cpuPowerUsageMap.get(computer) + process.getCpuRequired();
                cpuPowerUsageMap.put(computer, cpuPowerUsage);
                int memoryUsage = memoryUsageMap.get(computer) + process.getMemoryRequired();
                memoryUsageMap.put(computer, memoryUsage);
                int networkBandwidthUsage = networkBandwidthUsageMap.get(computer) + process.getNetworkRequired();
                networkBandwidthUsageMap.put(computer, networkBandwidthUsage);
                usedComputerSet.add(computer);
            }
        }
    }

    private int sumHardScore(Map<CloudComputer, Integer> cpuPowerUsageMap, Map<CloudComputer, Integer> memoryUsageMap,
                             Map<CloudComputer, Integer> networkBandwidthUsageMap) {
        int hardScore = 0;
        for (Map.Entry<CloudComputer, Integer> usageEntry : cpuPowerUsageMap.entrySet()) {
            CloudComputer computer = usageEntry.getKey();
            int cpuPowerAvailable = computer.getCpuCapacity() - usageEntry.getValue();
            if (cpuPowerAvailable < 0) {
                hardScore += cpuPowerAvailable;
            }
        }
        for (Map.Entry<CloudComputer, Integer> usageEntry : memoryUsageMap.entrySet()) {
            CloudComputer computer = usageEntry.getKey();
            int memoryAvailable = computer.getMemoryCapacity() - usageEntry.getValue();
            if (memoryAvailable < 0) {
                hardScore += memoryAvailable;
            }
        }
        for (Map.Entry<CloudComputer, Integer> usageEntry : networkBandwidthUsageMap.entrySet()) {
            CloudComputer computer = usageEntry.getKey();
            int networkBandwidthAvailable = computer.getNetworkCapacity() - usageEntry.getValue();
            if (networkBandwidthAvailable < 0) {
                hardScore += networkBandwidthAvailable;
            }
        }
        return hardScore;
    }

    private int sumMediumScore(List<CloudProcess> processSet) {
        int mediumScore = 0;
        for (CloudProcess cloudProcess : processSet) {
            if (cloudProcess.getCloudComputer() == null) {
                mediumScore -= cloudProcess.getDifficultyIndex();
            }
        }
        return mediumScore;
    }

    private int sumSoftScore(Set<CloudComputer> usedComputerSet) {
        int softScore = 0;
        for (CloudComputer usedComputer : usedComputerSet) {
            softScore -= usedComputer.getCost();
        }
        return softScore;
    }
}
