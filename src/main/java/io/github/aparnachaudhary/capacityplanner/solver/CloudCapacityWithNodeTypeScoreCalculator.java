package io.github.aparnachaudhary.capacityplanner.solver;

import io.github.aparnachaudhary.capacityplanner.domain.CloudBalance;
import io.github.aparnachaudhary.capacityplanner.domain.CloudComputer;
import io.github.aparnachaudhary.capacityplanner.domain.CloudProcess;
import io.github.aparnachaudhary.capacityplanner.domain.NodeType;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;

import java.util.*;

public class CloudCapacityWithNodeTypeScoreCalculator implements EasyScoreCalculator<CloudBalance> {

    @Override
    public HardMediumSoftScore calculateScore(CloudBalance cloudBalance) {

        int computerListSize = cloudBalance.getCloudComputers().size();
        Map<CloudComputer, Integer> cpuUsageMap = new HashMap<>(computerListSize);
        Map<NodeType, Map<CloudComputer, Integer>> cpuUsageByNodeTypeMap = new HashMap<>(computerListSize);
        Map<CloudComputer, Integer> memoryUsageMap = new HashMap<>(computerListSize);
        Map<CloudComputer, Integer> diskUsageMap = new HashMap<>(computerListSize);

        for (CloudComputer computer : cloudBalance.getCloudComputers()) {
            cpuUsageMap.put(computer, 0);
            memoryUsageMap.put(computer, 0);
            diskUsageMap.put(computer, 0);
            Map<CloudComputer, Integer> cloudComputerCPUMap = cpuUsageByNodeTypeMap.get(computer.getNodeType());
            if (cloudComputerCPUMap == null) {
                cloudComputerCPUMap = new HashMap<>(computerListSize);
            }
            cloudComputerCPUMap.put(computer, 0);
            cpuUsageByNodeTypeMap.put(computer.getNodeType(), cloudComputerCPUMap);
        }

        Set<CloudComputer> usedComputerSet = new HashSet<>(computerListSize);

        visitProcessList(memoryUsageMap, diskUsageMap, cpuUsageByNodeTypeMap, usedComputerSet, cloudBalance.getCloudProcesses());


        int hardScore = sumHardScore(memoryUsageMap, diskUsageMap, cpuUsageByNodeTypeMap);

        // assigned to wrong computer
        hardScore = wrongNodeTypeAssignment(hardScore, cloudBalance.getCloudProcesses());

        int mediumScore = sumMediumScore(cloudBalance.getCloudProcesses());
        int softScore = sumSoftScore(usedComputerSet);

        return HardMediumSoftScore.of(hardScore, mediumScore, softScore);
    }

    private void visitProcessList(Map<CloudComputer, Integer> memoryUsageMap, Map<CloudComputer, Integer> diskUsageMap,
                                  Map<NodeType, Map<CloudComputer, Integer>> cpuUsageByNodeTypeMap, Set<CloudComputer> usedComputerSet,
                                  List<CloudProcess> processList) {

        // We loop through the processList only once for performance
        for (CloudProcess process : processList) {
            CloudComputer computer = process.getCloudComputer();
            if (computer != null) {

                boolean nodeTypeConstraintMatched = computer.getNodeType().equals(process.getNodeTypeRequired());
                if (nodeTypeConstraintMatched) {
                    Map<CloudComputer, Integer> cpuUsageForNodeMap = cpuUsageByNodeTypeMap.get(computer.getNodeType());
                    int cpuUsageValue = cpuUsageForNodeMap.get(computer) + process.getCpuRequired();
                    cpuUsageForNodeMap.put(computer, cpuUsageValue);
                    cpuUsageByNodeTypeMap.put(computer.getNodeType(), cpuUsageForNodeMap);

                    int memoryUsage = memoryUsageMap.get(computer) + process.getMemoryRequired();
                    memoryUsageMap.put(computer, memoryUsage);

                    int diskUsage = diskUsageMap.get(computer) + process.getDiskRequired();
                    diskUsageMap.put(computer, diskUsage);

                    usedComputerSet.add(computer);
                }
            }
        }

    }

    private int sumHardScore(Map<CloudComputer, Integer> memoryUsageMap,
                             Map<CloudComputer, Integer> diskUsageMap, Map<NodeType, Map<CloudComputer, Integer>> cpuUsageByNodeTypeMap) {

        int hardScore = 0;

        // Assignment exceeds CPU capacity
        hardScore = hardScoreCpuCapacity(cpuUsageByNodeTypeMap, hardScore);

        // Assignment exceeds MEM capacity
//        hardScore = hardScoreMemoryCapacity(memoryUsageMap, hardScore);

        // Assignment exceeds disk capacity
//        hardScore = hardScoreDiskCapacity(diskUsageMap, hardScore);


        return hardScore;
    }

    private int hardScoreCpuCapacity(Map<NodeType, Map<CloudComputer, Integer>> cpuUsageByNodeTypeMap, int hardScore) {

        for (Map.Entry<NodeType, Map<CloudComputer, Integer>> usageEntry : cpuUsageByNodeTypeMap.entrySet()) {

            NodeType nodeType = usageEntry.getKey();
            Map<CloudComputer, Integer> nodeTypeCpuUsageMap = usageEntry.getValue();
            int nodeTypeCpuCapacity = nodeTypeCpuUsageMap.keySet().stream()
                    .filter(cloudComputer -> cloudComputer.getNodeType().equals(nodeType))
                    .mapToInt(CloudComputer::getCpuCapacity)
                    .sum();

            // Per Computer CPU Capacity
            hardScore += nodeTypeCpuUsageMap.entrySet().stream()
                    .mapToInt(entry -> entry.getKey().getCpuCapacity() - entry.getValue())
                    .filter(cpuPowerAvailable -> cpuPowerAvailable < 0)
                    .sum();

            // Per Node Type CPU Capacity
            int nodeTypeCpuUsage = nodeTypeCpuUsageMap.entrySet().stream()
                    .filter(entry -> entry.getKey().getNodeType().equals(nodeType))
                    .mapToInt(Map.Entry::getValue)
                    .sum();

            int nodeTypeCpuAvailable = nodeTypeCpuCapacity - nodeTypeCpuUsage;
            if (nodeTypeCpuAvailable < 0) {
                hardScore += nodeTypeCpuAvailable;
            }
        }
        return hardScore;
    }

    private int hardScoreMemoryCapacity(Map<CloudComputer, Integer> memoryUsageMap, int hardScore) {

        for (Map.Entry<CloudComputer, Integer> usageEntry : memoryUsageMap.entrySet()) {
            CloudComputer computer = usageEntry.getKey();
            int memoryAvailable = computer.getMemoryCapacity() - usageEntry.getValue();
            if (memoryAvailable < 0) {
                hardScore += memoryAvailable;
            }
        }
        return hardScore;
    }

    private int hardScoreDiskCapacity(Map<CloudComputer, Integer> diskUsageMap, int hardScore) {

        for (Map.Entry<CloudComputer, Integer> usageEntry : diskUsageMap.entrySet()) {
            CloudComputer computer = usageEntry.getKey();
            int diskAvailable = computer.getDiskCapacity() - usageEntry.getValue();
            if (diskAvailable < 0) {
                hardScore += diskAvailable;
            }
        }
        return hardScore;
    }

    private int sumMediumScore(List<CloudProcess> processSet) {

        int mediumScore = 0;

        for (CloudProcess cloudProcess : processSet) {

            // not assigned to any computer
            if (cloudProcess.getCloudComputer() == null) {
                mediumScore -= cloudProcess.getDifficultyIndex();
            }

        }

        return mediumScore;
    }

    private int wrongNodeTypeAssignment(int mediumScore, List<CloudProcess> processSet) {

        for (CloudProcess cloudProcess : processSet) {
            if (cloudProcess.getCloudComputer() != null && !cloudProcess.getNodeTypeRequired().equals(cloudProcess.getCloudComputer().getNodeType())) {
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
