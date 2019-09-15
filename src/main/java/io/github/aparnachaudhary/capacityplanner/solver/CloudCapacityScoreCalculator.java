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


        CloudUtilization resourceCapacity = cloudBalance.getResourceCapacity();

        visitProcessList(resourceCapacity, usedComputerSet, cloudBalance.getCloudProcesses());


        int hardScore = 0;
        int mediumScore = 0;
        int softScore = 0;

        // Per AZ CPU Capacity And Usage
        hardScore += cpuUsageAvailabilityZoneScore(resourceCapacity);
        // Per NodeType CPU Capacity And Usage
        hardScore += cpuUsageNodeTypeScore(resourceCapacity);
        // Per Computer CPU Capacity And Usage
        hardScore += cpuUsageComputerScore(resourceCapacity.getNodeResourceUsageMap());
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


    private void visitProcessList(CloudUtilization resourceCapacity, Set<CloudComputer> usedComputerSet,
                                  List<CloudProcess> processList) {

        // We loop through the processList only once for performance
        for (CloudProcess process : processList) {
            CloudComputer computer = process.getCloudComputer();

            if (computer != null) {

                boolean nodeTypeConstraintMatched = computer.getNodeType().equals(process.getNodeTypeRequired());
                boolean azConstraintMatched = computer.getAvailabilityZone().equals(process.getAvailabilityZoneRequired());

                if (nodeTypeConstraintMatched && azConstraintMatched) {
                    Map<CloudComputer, ResourceUsage> nodeUsageMap = resourceCapacity.getNodeResourceUsageMap();
                    int cpuUsage = nodeUsageMap.get(computer).getCpuUsage() + process.getCpuRequired();
                    int memUsage = nodeUsageMap.get(computer).getMemoryUsage() + process.getMemoryRequired();
                    int diskUsage = nodeUsageMap.get(computer).getDiskUsage() + process.getDiskRequired();
                    ResourceUsage resourceUsage = ResourceUsage.builder()
                            .cpuUsage(cpuUsage)
                            .memoryUsage(memUsage)
                            .diskUsage(diskUsage)
                            .build();
                    nodeUsageMap.put(computer, resourceUsage);
                    // Add computer to used set
                    usedComputerSet.add(computer);
                }
            }
        }

    }

    private int cpuUsageAvailabilityZoneScore(CloudUtilization resourceCapacity) {

        int hardScore = 0;

        Map<AvailabilityZone, ResourceCapacity> azResourceCapacityMap = resourceCapacity.getAzResourceCapacityMap();
        Map<AvailabilityZone, ResourceUsage> azResourceUsageMap = resourceCapacity.getAzResourceUsageMap();
        Map<CloudComputer, ResourceUsage> nodeUsageMap = resourceCapacity.getNodeResourceUsageMap();


        for (CloudComputer computer : nodeUsageMap.keySet()) {

            AvailabilityZone availabilityZone = computer.getAvailabilityZone();
            // Per AZ CPU Usage
            ResourceUsage resourceUsage = azResourceUsageMap.get(availabilityZone);
            int azCpuUsage = resourceUsage.getCpuUsage() + nodeUsageMap.get(computer).getCpuUsage();
            resourceUsage.setCpuUsage(azCpuUsage);
            // Per AZ CPU Capacity
            int azCpuCapacity = azResourceCapacityMap.get(availabilityZone).getCpuCapacity();
            // Per AZ CPU Capacity And Usage
            int azCpuAvailable = azCpuCapacity - azCpuUsage;
            if (azCpuAvailable < 0) {
                hardScore += azCpuAvailable;
            }
        }

        return hardScore;
    }

    private int cpuUsageNodeTypeScore(CloudUtilization resourceCapacity) {

        int hardScore = 0;

        Map<NodeType, ResourceCapacity> nodeTypeResourceCapacityMap = resourceCapacity.getNodeTypeResourceCapacityMap();
        Map<NodeType, ResourceUsage> nodeTypeResourceUsageMap = resourceCapacity.getNodeTypeResourceUsageMap();
        Map<CloudComputer, ResourceUsage> nodeUsageMap = resourceCapacity.getNodeResourceUsageMap();


        for (CloudComputer computer : nodeUsageMap.keySet()) {

            NodeType nodeType = computer.getNodeType();
            // Per NodeType CPU Usage
            ResourceUsage resourceUsage = nodeTypeResourceUsageMap.get(nodeType);
            int nodeTypeCpuUsage = resourceUsage.getCpuUsage() + nodeUsageMap.get(computer).getCpuUsage();
            resourceUsage.setCpuUsage(nodeTypeCpuUsage);
            // Per NodeType CPU Capacity
            int nodeTypeCpuCapacity = nodeTypeResourceCapacityMap.get(nodeType).getCpuCapacity();
            // Per NodeType CPU Capacity And Usage
            int nodeTypeCpuAvailable = nodeTypeCpuCapacity - nodeTypeCpuUsage;
            if (nodeTypeCpuAvailable < 0) {
                hardScore += nodeTypeCpuAvailable;
            }
        }

        return hardScore;
    }

    private int cpuUsageComputerScore(Map<CloudComputer, ResourceUsage> nodeResourceUsageMap) {

        int score = 0;

        for (Map.Entry<CloudComputer, ResourceUsage> usageEntry : nodeResourceUsageMap.entrySet()) {
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
