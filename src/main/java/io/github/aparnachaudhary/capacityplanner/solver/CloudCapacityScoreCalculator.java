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
                boolean azConstraintMatched = clusterNode.getAvailabilityZone().equals(process.getAvailabilityZone());

                if (nodeTypeConstraintMatched && azConstraintMatched) {
                    Map<ClusterNode, ResourceUsage> nodeResourceUsageMap = resourceCapacity.getNodeResourceUsageMap();
                    ResourceUsage existingResourceUsage = nodeResourceUsageMap.get(clusterNode);
                    ResourceUsage resourceUsage = ResourceUsage.builder()
                            .cpuUsage(existingResourceUsage.getCpuUsage() + process.getCpu())
                            .memoryUsage(existingResourceUsage.getMemoryUsage() + process.getMemory())
                            .diskUsage(existingResourceUsage.getDiskUsage() + process.getDisk())
                            .build();
                    nodeResourceUsageMap.put(clusterNode, resourceUsage);
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
            ResourceUsage clusterNodeResourceUsage = usageEntry.getValue();

            AvailabilityZone availabilityZone = clusterNode.getAvailabilityZone();
            ClusterNodeType nodeType = clusterNode.getClusterNodeType();

            // Per AZ Resource Usage
            ResourceUsage availabilityZoneResourceUsage = clusterUtilization.getAzResourceUsageMap().get(availabilityZone);
            availabilityZoneResourceUsage.setCpuUsage(availabilityZoneResourceUsage.getCpuUsage() + clusterNodeResourceUsage.getCpuUsage());
            availabilityZoneResourceUsage.setMemoryUsage(availabilityZoneResourceUsage.getMemoryUsage() + clusterNodeResourceUsage.getMemoryUsage());
            availabilityZoneResourceUsage.setDiskUsage(availabilityZoneResourceUsage.getDiskUsage() + clusterNodeResourceUsage.getDiskUsage());
            // Per AZ Resource Capacity
            ResourceCapacity availabilityZoneResourceCapacity = clusterUtilization.getAzResourceCapacityMap().get(availabilityZone);

            // Per AZ CPU Capacity And Usage
            if (availabilityZoneResourceCapacity.getCpuCapacity() - availabilityZoneResourceUsage.getCpuUsage() < 0) {
                score += availabilityZoneResourceCapacity.getCpuCapacity() - availabilityZoneResourceUsage.getCpuUsage();
            }
            if (availabilityZoneResourceCapacity.getMemoryCapacity() - availabilityZoneResourceUsage.getMemoryUsage() < 0) {
                score += availabilityZoneResourceCapacity.getMemoryCapacity() - availabilityZoneResourceUsage.getMemoryUsage();
            }
            if (availabilityZoneResourceCapacity.getDiskCapacity() - availabilityZoneResourceUsage.getDiskUsage() < 0) {
                score += availabilityZoneResourceCapacity.getDiskCapacity() - availabilityZoneResourceUsage.getDiskUsage();
            }

            // Per ClusterNodeType Resource Usage
            ResourceUsage nodeTypeResourceUsage = clusterUtilization.getNodeTypeResourceUsageMap().get(nodeType);
            nodeTypeResourceUsage.setCpuUsage(nodeTypeResourceUsage.getCpuUsage() + clusterNodeResourceUsage.getCpuUsage());
            nodeTypeResourceUsage.setMemoryUsage(nodeTypeResourceUsage.getMemoryUsage() + clusterNodeResourceUsage.getMemoryUsage());
            nodeTypeResourceUsage.setDiskUsage(nodeTypeResourceUsage.getDiskUsage() + clusterNodeResourceUsage.getDiskUsage());

            // Per ClusterNodeType Resource Capacity
            ResourceCapacity nodeTypeResourceCapacity = clusterUtilization.getNodeTypeResourceCapacityMap().get(nodeType);
            // Per ClusterNodeType CPU Capacity And Usage
            if (nodeTypeResourceCapacity.getCpuCapacity() - nodeTypeResourceUsage.getCpuUsage() < 0) {
                score += nodeTypeResourceCapacity.getCpuCapacity() - nodeTypeResourceUsage.getCpuUsage();
            }
            if (nodeTypeResourceCapacity.getMemoryCapacity() - nodeTypeResourceUsage.getMemoryUsage() < 0) {
                score += nodeTypeResourceCapacity.getMemoryCapacity() - nodeTypeResourceUsage.getMemoryUsage();
            }
            if (nodeTypeResourceCapacity.getDiskCapacity() - nodeTypeResourceUsage.getDiskUsage() < 0) {
                score += nodeTypeResourceCapacity.getDiskCapacity() - nodeTypeResourceUsage.getDiskUsage();
            }

            // Per ClusterNode Resource Capacity And Usage
            if (clusterNode.getCpu() - clusterNodeResourceUsage.getCpuUsage() < 0) {
                score += clusterNode.getCpu() - clusterNodeResourceUsage.getCpuUsage();
            }
            if (clusterNode.getMemory() - clusterNodeResourceUsage.getMemoryUsage() < 0) {
                score += clusterNode.getMemory() - clusterNodeResourceUsage.getMemoryUsage();
            }
            if (clusterNode.getDisk() - clusterNodeResourceUsage.getDiskUsage() < 0) {
                score += clusterNode.getDisk() - clusterNodeResourceUsage.getDiskUsage();
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
            if (clusterProcess.getClusterNode() != null && !clusterProcess.getAvailabilityZone().equals(clusterProcess.getClusterNode().getAvailabilityZone())) {
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
