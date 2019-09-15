package io.github.aparnachaudhary.capacityplanner.solver;

import io.github.aparnachaudhary.capacityplanner.domain.*;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;

import java.util.*;

public class CloudCapacityScoreCalculator implements EasyScoreCalculator<ClusterBalance> {

    @Override
    public HardMediumSoftScore calculateScore(ClusterBalance clusterBalance) {

        int clusterNodeSize = clusterBalance.getClusterNodes().size();
        Set<ClusterNode> usedClusterNodes = new HashSet<>(clusterNodeSize);


        ClusterUtilization resourceCapacity = clusterBalance.getResourceCapacity();

        visitProcessList(resourceCapacity, usedClusterNodes, clusterBalance.getClusterProcesses());


        int hardScore = 0;
        int mediumScore = 0;
        int softScore = 0;

        // Per AZ/NodeType/Node Resource Capacity And Usage
        hardScore += resourceAllocationScore(resourceCapacity);
        // Per ClusterNode CPU Capacity And Usage
        hardScore += cpuUsageClusterNodeScore(resourceCapacity.getNodeResourceUsageMap());
        // Assigned to Wrong ClusterNodeType
        hardScore += wrongNodeTypeAssignment(clusterBalance.getClusterProcesses());
        // Assigned to Wrong AZ
        hardScore += wrongAZAssignment(clusterBalance.getClusterProcesses());
        // Not Assigned to Any Cluster Node
        mediumScore += notAssignedToClusterNode(clusterBalance.getClusterProcesses());
        // Cost incurred based on Cluster Nodes Used
        softScore += usedClusterNodesCostScore(usedClusterNodes);

        return HardMediumSoftScore.of(hardScore, mediumScore, softScore);
    }


    private void visitProcessList(ClusterUtilization resourceCapacity, Set<ClusterNode> usedClusterNodes,
                                  List<ClusterProcess> processList) {

        // We loop through the processList only once for performance
        for (ClusterProcess process : processList) {
            ClusterNode clusterNode = process.getClusterNode();

            if (clusterNode != null) {

                boolean nodeTypeConstraintMatched = clusterNode.getClusterNodeType().equals(process.getClusterNodeType());
                boolean azConstraintMatched = clusterNode.getAvailabilityZone().equals(process.getAvailabilityZoneRequired());

                if (nodeTypeConstraintMatched && azConstraintMatched) {
                    Map<ClusterNode, ResourceUsage> nodeUsageMap = resourceCapacity.getNodeResourceUsageMap();
                    int cpuUsage = nodeUsageMap.get(clusterNode).getCpuUsage() + process.getCpuRequired();
                    int memUsage = nodeUsageMap.get(clusterNode).getMemoryUsage() + process.getMemoryRequired();
                    int diskUsage = nodeUsageMap.get(clusterNode).getDiskUsage() + process.getDiskRequired();
                    ResourceUsage resourceUsage = ResourceUsage.builder()
                            .cpuUsage(cpuUsage)
                            .memoryUsage(memUsage)
                            .diskUsage(diskUsage)
                            .build();
                    nodeUsageMap.put(clusterNode, resourceUsage);
                    // Add clusterNode to used set
                    usedClusterNodes.add(clusterNode);
                }
            }
        }

    }

    private int resourceAllocationScore(ClusterUtilization clusterUtilization) {

        int score = 0;

        for (Map.Entry<ClusterNode, ResourceUsage> usageEntry : clusterUtilization.getNodeResourceUsageMap().entrySet()) {

            ClusterNode clusterNode = usageEntry.getKey();
            ResourceUsage clusterNodeResourceCapacity = usageEntry.getValue();

            AvailabilityZone availabilityZone = clusterNode.getAvailabilityZone();
            ClusterNodeType nodeType = clusterNode.getClusterNodeType();

            // Per AZ Resource Usage
            ResourceUsage availabilityZoneResourceUsage = clusterUtilization.getAzResourceUsageMap().get(availabilityZone);
            availabilityZoneResourceUsage.setCpuUsage(availabilityZoneResourceUsage.getCpuUsage() + clusterNodeResourceCapacity.getCpuUsage());
            availabilityZoneResourceUsage.setMemoryUsage(availabilityZoneResourceUsage.getMemoryUsage() + clusterNodeResourceCapacity.getMemoryUsage());
            availabilityZoneResourceUsage.setDiskUsage(availabilityZoneResourceUsage.getDiskUsage() + clusterNodeResourceCapacity.getDiskUsage());
            // Per AZ Resource Capacity
            ResourceCapacity availabilityZoneResourceCapacity = clusterUtilization.getAzResourceCapacityMap().get(availabilityZone);

            // Per AZ CPU Capacity And Usage
            if (availabilityZoneResourceCapacity.getCpuCapacity() - availabilityZoneResourceUsage.getCpuUsage() < 0) {
                score += availabilityZoneResourceCapacity.getCpuCapacity() - availabilityZoneResourceUsage.getCpuUsage();
            }

            // Per ClusterNodeType Resource Usage
            ResourceUsage nodeTypeResourceUsage = clusterUtilization.getNodeTypeResourceUsageMap().get(nodeType);
            nodeTypeResourceUsage.setCpuUsage(nodeTypeResourceUsage.getCpuUsage() + clusterNodeResourceCapacity.getCpuUsage());
            nodeTypeResourceUsage.setMemoryUsage(nodeTypeResourceUsage.getMemoryUsage() + clusterNodeResourceCapacity.getMemoryUsage());
            nodeTypeResourceUsage.setDiskUsage(nodeTypeResourceUsage.getDiskUsage() + clusterNodeResourceCapacity.getDiskUsage());

            // Per ClusterNodeType Resource Capacity
            ResourceCapacity nodeTypeResourceCapacity = clusterUtilization.getNodeTypeResourceCapacityMap().get(nodeType);
            // Per ClusterNodeType CPU Capacity And Usage
            if (nodeTypeResourceCapacity.getCpuCapacity() - nodeTypeResourceUsage.getCpuUsage() < 0) {
                score += nodeTypeResourceCapacity.getCpuCapacity() - nodeTypeResourceUsage.getCpuUsage();
            }

            //
        }

        return score;
    }

    private int cpuUsageClusterNodeScore(Map<ClusterNode, ResourceUsage> nodeResourceUsageMap) {

        int score = 0;

        for (Map.Entry<ClusterNode, ResourceUsage> usageEntry : nodeResourceUsageMap.entrySet()) {
            ClusterNode clusterNode = usageEntry.getKey();
            int cpuAvailable = clusterNode.getCpuCapacity() - usageEntry.getValue().getCpuUsage();
            if (cpuAvailable < 0) {
                score += cpuAvailable;
            }
        }
        return score;
    }

    private int notAssignedToClusterNode(List<ClusterProcess> processSet) {

        int score = 0;

        for (ClusterProcess clusterProcess : processSet) {
            // not assigned to any cluster node
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

    private int usedClusterNodesCostScore(Set<ClusterNode> usedClusterNodes) {

        int score = 0;
        for (ClusterNode usedClusterNode : usedClusterNodes) {
            score -= usedClusterNode.getCost();
        }
        return score;
    }
}
