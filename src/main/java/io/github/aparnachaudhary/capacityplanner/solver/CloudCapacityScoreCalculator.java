package io.github.aparnachaudhary.capacityplanner.solver;

import io.github.aparnachaudhary.capacityplanner.domain.*;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;

import java.util.*;

public class CloudCapacityScoreCalculator implements EasyScoreCalculator<CloudBalance> {

    @Override
    public HardMediumSoftScore calculateScore(CloudBalance cloudBalance) {

        int computerListSize = cloudBalance.getCloudComputers().size();
        Set<CloudComputer> usedComputerSet = new HashSet<>(computerListSize);


        ResourceCapacity resourceCapacity = cloudBalance.getResourceCapacity();

        visitProcessList(resourceCapacity, usedComputerSet, cloudBalance.getCloudProcesses());


        int hardScore = 0;
        int mediumScore = 0;
        int softScore = 0;

        // Per AZ CPU Capacity And Usage
        hardScore += cpuUsageAvailabilityZoneScore(resourceCapacity);
        // Per NodeType CPU Capacity And Usage
        hardScore += cpuUsageNodeTypeScore(resourceCapacity);
        // Per Computer CPU Capacity And Usage
        hardScore += cpuUsageComputerScore(resourceCapacity.getNodeUsageMap());
        // Assigned to Wrong NodeType
        hardScore += wrongNodeTypeAssignment(cloudBalance.getCloudProcesses());
        // Assigned to Wrong AZ
        hardScore += wrongAZAssignment(cloudBalance.getCloudProcesses());
        // Not Assigned to Any Computer
        mediumScore += notAssignedToComputer(cloudBalance.getCloudProcesses());
        // Cost incurred based on Computers Used
        softScore += usedComputerCostScore(usedComputerSet);

        return HardMediumSoftScore.of(hardScore, mediumScore, softScore);
    }


    private void visitProcessList(ResourceCapacity resourceCapacity, Set<CloudComputer> usedComputerSet,
                                  List<CloudProcess> processList) {

        // We loop through the processList only once for performance
        for (CloudProcess process : processList) {
            CloudComputer computer = process.getCloudComputer();

            if (computer != null) {

                boolean nodeTypeConstraintMatched = computer.getNodeType().equals(process.getNodeTypeRequired());
                boolean azConstraintMatched = computer.getAvailabilityZone().equals(process.getAvailabilityZoneRequired());

                if (nodeTypeConstraintMatched && azConstraintMatched) {
                    Map<CloudComputer, NodeResourceUsage> nodeUsageMap = resourceCapacity.getNodeUsageMap();
                    int cpuUsage = nodeUsageMap.get(computer).getCpuUsage() + process.getCpuRequired();
                    int memUsage = nodeUsageMap.get(computer).getMemoryUsage() + process.getMemoryRequired();
                    int diskUsage = nodeUsageMap.get(computer).getDiskUsage() + process.getDiskRequired();
                    NodeResourceUsage nodeResourceUsage = NodeResourceUsage.builder()
                            .cpuUsage(cpuUsage)
                            .memoryUsage(memUsage)
                            .diskUsage(diskUsage)
                            .build();
                    nodeUsageMap.put(computer, nodeResourceUsage);
                    // Add computer to used set
                    usedComputerSet.add(computer);
                }
            }
        }

    }

    private int cpuUsageAvailabilityZoneScore(ResourceCapacity resourceCapacity) {

        int hardScore = 0;

        Map<AvailabilityZone, Integer> azCpuCapacityMap = resourceCapacity.getAzCpuCapacityMap();
        Map<AvailabilityZone, Integer> azCpuUsageMap = resourceCapacity.getAzCpuUsageMap();
        Map<CloudComputer, NodeResourceUsage> nodeUsageMap = resourceCapacity.getNodeUsageMap();


        for (CloudComputer computer : nodeUsageMap.keySet()) {

            AvailabilityZone availabilityZone = computer.getAvailabilityZone();

            // Per AZ CPU Usage
            int azCpuUsage = azCpuUsageMap.get(availabilityZone) + nodeUsageMap.get(computer).getCpuUsage();
            // Per AZ CPU Capacity
            int azCpuCapacity = azCpuCapacityMap.get(availabilityZone);
            azCpuUsageMap.put(availabilityZone, azCpuUsage);
            // Per AZ CPU Capacity And Usage
            int nodeTypeCpuAvailable = azCpuCapacity - azCpuUsage;
            if (nodeTypeCpuAvailable < 0) {
                hardScore += nodeTypeCpuAvailable;
            }

        }

        return hardScore;
    }

    private int cpuUsageNodeTypeScore(ResourceCapacity resourceCapacity) {

        int hardScore = 0;

        Map<NodeType, Integer> nodeTypeCpuCapacityMap = resourceCapacity.getNodeTypeCpuCapacityMap();
        Map<NodeType, Integer> nodeTypeCpuUsageMap = resourceCapacity.getNodeTypeCpuUsageMap();
        Map<CloudComputer, NodeResourceUsage> nodeUsageMap = resourceCapacity.getNodeUsageMap();


        for (CloudComputer computer : nodeUsageMap.keySet()) {

            NodeType nodeType = computer.getNodeType();

            // Per NodeType CPU Usage
            int nodeTypeCpuUsage = nodeTypeCpuUsageMap.get(nodeType) + nodeUsageMap.get(computer).getCpuUsage();
            // Per NodeType CPU Capacity
            int nodeTypeCpuCapacity = nodeTypeCpuCapacityMap.get(nodeType);
            nodeTypeCpuUsageMap.put(nodeType, nodeTypeCpuUsage);
            // Per NodeType CPU Capacity And Usage
            int nodeTypeCpuAvailable = nodeTypeCpuCapacity - nodeTypeCpuUsage;
            if (nodeTypeCpuAvailable < 0) {
                hardScore += nodeTypeCpuAvailable;
            }

        }

        return hardScore;
    }

    private int cpuUsageComputerScore(Map<CloudComputer, NodeResourceUsage> nodeResourceUsageMap) {

        int score = 0;

        for (Map.Entry<CloudComputer, NodeResourceUsage> usageEntry : nodeResourceUsageMap.entrySet()) {
            CloudComputer computer = usageEntry.getKey();
            int cpuAvailable = computer.getCpuCapacity() - usageEntry.getValue().getCpuUsage();
            if (cpuAvailable < 0) {
                score += cpuAvailable;
            }
        }
        return score;
    }

    private int notAssignedToComputer(List<CloudProcess> processSet) {

        int score = 0;

        for (CloudProcess cloudProcess : processSet) {
            // not assigned to any computer
            if (cloudProcess.getCloudComputer() == null) {
                score -= cloudProcess.getDifficultyIndex();
            }

        }

        return score;
    }

    private int wrongAZAssignment(List<CloudProcess> processSet) {

        int score = 0;

        for (CloudProcess cloudProcess : processSet) {
            if (cloudProcess.getCloudComputer() != null && !cloudProcess.getAvailabilityZoneRequired().equals(cloudProcess.getCloudComputer().getAvailabilityZone())) {
                score -= cloudProcess.getDifficultyIndex();
            }
        }
        return score;
    }

    private int wrongNodeTypeAssignment(List<CloudProcess> processSet) {

        int score = 0;

        for (CloudProcess cloudProcess : processSet) {
            if (cloudProcess.getCloudComputer() != null && !cloudProcess.getNodeTypeRequired().equals(cloudProcess.getCloudComputer().getNodeType())) {
                score -= cloudProcess.getDifficultyIndex();
            }
        }
        return score;
    }

    private int usedComputerCostScore(Set<CloudComputer> usedComputerSet) {

        int score = 0;
        for (CloudComputer usedComputer : usedComputerSet) {
            score -= usedComputer.getCost();
        }
        return score;
    }
}
