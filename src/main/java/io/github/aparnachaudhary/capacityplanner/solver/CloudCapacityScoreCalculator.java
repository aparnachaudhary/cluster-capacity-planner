package io.github.aparnachaudhary.capacityplanner.solver;

import io.github.aparnachaudhary.capacityplanner.domain.*;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;

import java.util.*;

public class CloudCapacityScoreCalculator implements EasyScoreCalculator<ClusterBalance> {

    @Override
    public HardMediumSoftScore calculateScore(ClusterBalance clusterBalance) {

        int computerListSize = clusterBalance.getClusterNodes().size();
        Set<ClusterNode> usedComputerSet = new HashSet<>(computerListSize);


        CloudUtilization resourceCapacity = clusterBalance.getResourceCapacity();

        visitProcessList(resourceCapacity, usedComputerSet, clusterBalance.getClusterProcesses());


        int hardScore = 0;
        int mediumScore = 0;
        int softScore = 0;

        // Per AZ CPU Capacity And Usage
        hardScore += cpuUsageAvailabilityZoneScore(resourceCapacity);
        // Per ClusterNodeType CPU Capacity And Usage
        hardScore += cpuUsageNodeTypeScore(resourceCapacity);
        // Per Computer CPU Capacity And Usage
        hardScore += cpuUsageComputerScore(resourceCapacity.getNodeResourceUsageMap());
        // Assigned to Wrong ClusterNodeType
        hardScore += wrongNodeTypeAssignment(clusterBalance.getClusterProcesses());
        // Assigned to Wrong AZ
        hardScore += wrongAZAssignment(clusterBalance.getClusterProcesses());
        // Not Assigned to Any Computer
        mediumScore += notAssignedToComputer(clusterBalance.getClusterProcesses());
        // Cost incurred based on Computers Used
        softScore += usedComputerCostScore(usedComputerSet);

        return HardMediumSoftScore.of(hardScore, mediumScore, softScore);
    }


    private void visitProcessList(CloudUtilization resourceCapacity, Set<ClusterNode> usedComputerSet,
                                  List<ClusterProcess> processList) {

        // We loop through the processList only once for performance
        for (ClusterProcess process : processList) {
            ClusterNode computer = process.getClusterNode();

            if (computer != null) {

                boolean nodeTypeConstraintMatched = computer.getClusterNodeType().equals(process.getClusterNodeType());
                boolean azConstraintMatched = computer.getAvailabilityZone().equals(process.getAvailabilityZoneRequired());

                if (nodeTypeConstraintMatched && azConstraintMatched) {
                    Map<ClusterNode, ResourceUsage> nodeUsageMap = resourceCapacity.getNodeResourceUsageMap();
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
        Map<ClusterNode, ResourceUsage> nodeUsageMap = resourceCapacity.getNodeResourceUsageMap();


        for (ClusterNode computer : nodeUsageMap.keySet()) {

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

        Map<ClusterNodeType, ResourceCapacity> nodeTypeResourceCapacityMap = resourceCapacity.getNodeTypeResourceCapacityMap();
        Map<ClusterNodeType, ResourceUsage> nodeTypeResourceUsageMap = resourceCapacity.getNodeTypeResourceUsageMap();
        Map<ClusterNode, ResourceUsage> nodeUsageMap = resourceCapacity.getNodeResourceUsageMap();


        for (ClusterNode computer : nodeUsageMap.keySet()) {

            ClusterNodeType nodeType = computer.getClusterNodeType();
            // Per ClusterNodeType CPU Usage
            ResourceUsage resourceUsage = nodeTypeResourceUsageMap.get(nodeType);
            int nodeTypeCpuUsage = resourceUsage.getCpuUsage() + nodeUsageMap.get(computer).getCpuUsage();
            resourceUsage.setCpuUsage(nodeTypeCpuUsage);
            // Per ClusterNodeType CPU Capacity
            int nodeTypeCpuCapacity = nodeTypeResourceCapacityMap.get(nodeType).getCpuCapacity();
            // Per ClusterNodeType CPU Capacity And Usage
            int nodeTypeCpuAvailable = nodeTypeCpuCapacity - nodeTypeCpuUsage;
            if (nodeTypeCpuAvailable < 0) {
                hardScore += nodeTypeCpuAvailable;
            }
        }

        return hardScore;
    }

    private int cpuUsageComputerScore(Map<ClusterNode, ResourceUsage> nodeResourceUsageMap) {

        int score = 0;

        for (Map.Entry<ClusterNode, ResourceUsage> usageEntry : nodeResourceUsageMap.entrySet()) {
            ClusterNode computer = usageEntry.getKey();
            int cpuAvailable = computer.getCpuCapacity() - usageEntry.getValue().getCpuUsage();
            if (cpuAvailable < 0) {
                score += cpuAvailable;
            }
        }
        return score;
    }

    private int notAssignedToComputer(List<ClusterProcess> processSet) {

        int score = 0;

        for (ClusterProcess clusterProcess : processSet) {
            // not assigned to any computer
            if (clusterProcess.getClusterNode() == null) {
                score -= clusterProcess.getDifficultyIndex();
            }

        }

        return score;
    }

    private int wrongAZAssignment(List<ClusterProcess> processSet) {

        int score = 0;

        for (ClusterProcess clusterProcess : processSet) {
            if (clusterProcess.getClusterNode() != null && !clusterProcess.getAvailabilityZoneRequired().equals(clusterProcess.getClusterNode().getAvailabilityZone())) {
                score -= clusterProcess.getDifficultyIndex();
            }
        }
        return score;
    }

    private int wrongNodeTypeAssignment(List<ClusterProcess> processSet) {

        int score = 0;

        for (ClusterProcess clusterProcess : processSet) {
            if (clusterProcess.getClusterNode() != null && !clusterProcess.getClusterNodeType().equals(clusterProcess.getClusterNode().getClusterNodeType())) {
                score -= clusterProcess.getDifficultyIndex();
            }
        }
        return score;
    }

    private int usedComputerCostScore(Set<ClusterNode> usedComputerSet) {

        int score = 0;
        for (ClusterNode usedComputer : usedComputerSet) {
            score -= usedComputer.getCost();
        }
        return score;
    }
}
