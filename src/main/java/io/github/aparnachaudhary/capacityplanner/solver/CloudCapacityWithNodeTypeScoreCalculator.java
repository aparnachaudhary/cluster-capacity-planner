package io.github.aparnachaudhary.capacityplanner.solver;

import io.github.aparnachaudhary.capacityplanner.domain.*;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;

import java.util.*;

public class CloudCapacityWithNodeTypeScoreCalculator implements EasyScoreCalculator<CloudBalance> {

    @Override
    public HardMediumSoftScore calculateScore(CloudBalance cloudBalance) {

        int computerListSize = cloudBalance.getCloudComputers().size();
        Map<AvailabilityZone, Map<NodeType, CloudComputer>> computersByAZAndNodeType = cloudBalance.getComputersByAZAndNodeType();
        Set<CloudComputer> usedComputerSet = new HashSet<>(computerListSize);


        ResourceCapacity resourceCapacity = cloudBalance.getResourceCapacity();

        visitProcessList(resourceCapacity, usedComputerSet, cloudBalance.getCloudProcesses());


        int hardScore = 0;
        int mediumScore = 0;
        int softScore = 0;

        // Per AZ CPU Capacity And Usage
        hardScore += cpuUsageAZExceedsCapacityScore(computersByAZAndNodeType, resourceCapacity);
        // Per NodeType CPU Capacity And Usage

        // Per Computer CPU Capacity And Usage
        hardScore += cpuUsageComputerScore(resourceCapacity.getCpuUsageMap());
        // Assigned to Wrong Computer
        hardScore += wrongNodeTypeAssignment(cloudBalance.getCloudProcesses());
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
                    Map<CloudComputer, Integer> cpuUsageMap = resourceCapacity.getCpuUsageMap();
                    int cpuPowerUsage = cpuUsageMap.get(computer) + process.getCpuRequired();
                    cpuUsageMap.put(computer, cpuPowerUsage);
                    // Add computer to used set
                    usedComputerSet.add(computer);
                }
            }
        }

    }

    private int cpuUsageAZExceedsCapacityScore(Map<AvailabilityZone, Map<NodeType, CloudComputer>> computersByAZAndNodeType, ResourceCapacity resourceCapacity) {

        int hardScore = 0;

        Map<AvailabilityZone, Integer> azCpuCapacityMap = resourceCapacity.getAzCpuCapacityMap();
        Map<AvailabilityZone, Integer> azCpuUsageMap = resourceCapacity.getAzCpuUsageMap();

        Map<NodeType, Integer> nodeTypeCpuCapacityMap = resourceCapacity.getNodeTypeCpuCapacityMap();
        Map<NodeType, Integer> nodeTypeCpuUsageMap = resourceCapacity.getNodeTypeCpuUsageMap();

        Map<CloudComputer, Integer> cpuUsageMap = resourceCapacity.getCpuUsageMap();


        for (Map.Entry<AvailabilityZone, Map<NodeType, CloudComputer>> azEntry : computersByAZAndNodeType.entrySet()) {

            AvailabilityZone availabilityZone = azEntry.getKey();
            Map<NodeType, CloudComputer> computersByNodeType = azEntry.getValue();

//            azCpuUsageMap.putIfAbsent(availabilityZone, 0);
//
//            for (NodeType nodeType : computersByNodeType.keySet()) {
//                nodeTypeCpuUsageMap.putIfAbsent(nodeType, 0);
//            }


            for (Map.Entry<NodeType, CloudComputer> entry : computersByNodeType.entrySet()) {

                NodeType nodeType = entry.getKey();
                CloudComputer computer = entry.getValue();

                // Per NodeType CPU Usage
                int nodeTypeCpuUsage = nodeTypeCpuUsageMap.get(nodeType) + cpuUsageMap.get(computer);
                int nodeTypeCpuCapacity = nodeTypeCpuCapacityMap.get(nodeType);
                nodeTypeCpuUsageMap.put(nodeType, nodeTypeCpuUsage);

                // Per NodeType CPU Capacity And Usage
                int nodeTypeCpuAvailable = nodeTypeCpuCapacity - nodeTypeCpuUsage;
                if (nodeTypeCpuAvailable < 0) {
                    hardScore += nodeTypeCpuAvailable;
                }

            }

//            // Per AZ CPU Capacity
//            int azCpuCapacity = nodeTypeCpuUsageMap.values().stream()
//                    .filter(cloudComputer -> cloudComputer.getAvailabilityZone().equals(availabilityZone))
//                    .mapToInt(CloudComputer::getCpuCapacity)
//                    .sum();
//
//            // Per AZ CPU Usage
//            int azCpuUsage = nodeTypeCpuUsageMap.values().stream()
//                    .filter(cloudComputer -> cloudComputer.getAvailabilityZone().equals(availabilityZone))
//                    .mapToInt(CloudComputer::getCpuUsage)
//                    .sum();
//
//
//            // Per AZ CPU Capacity And Usage
//            int azCpuAvailable = azCpuCapacity - azCpuUsage;
//            if (azCpuAvailable < 0) {
//                hardScore += azCpuAvailable;
//            }
        }

        return hardScore;
    }

    private int cpuUsageComputerScore(Map<CloudComputer, Integer> cpuCapacityUsageMap) {

        int score = 0;

        for (Map.Entry<CloudComputer, Integer> usageEntry : cpuCapacityUsageMap.entrySet()) {
            CloudComputer computer = usageEntry.getKey();
            int cpuPowerAvailable = computer.getCpuCapacity() - usageEntry.getValue();
            if (cpuPowerAvailable < 0) {
                score += cpuPowerAvailable;
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
