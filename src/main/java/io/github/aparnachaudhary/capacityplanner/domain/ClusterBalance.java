package io.github.aparnachaudhary.capacityplanner.domain;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;

import javax.persistence.Id;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@PlanningSolution
@Data
@Builder
public class ClusterBalance implements Serializable, Comparable<ClusterBalance> {

    @PlanningId
    @Id
    protected Long id;

    @PlanningScore
    protected HardMediumSoftScore score;

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "clusterNodeProvider")
    private List<ClusterNode> clusterNodes;

    @PlanningEntityCollectionProperty
    private List<ClusterProcess> clusterProcesses;
    private List<AvailabilityZone> availabilityZones;
    private List<ClusterNodeType> nodeTypes;


    @Override
    public int compareTo(ClusterBalance o) {
        return new CompareToBuilder().append(getClass().getName(), o.getClass().getName())
                .append(id, o.id).toComparison();
    }

    @Override
    public String toString() {
        return getClass().getName().replaceAll(".*\\.", "") + "-" + id;
    }

    public ClusterUtilization getResourceCapacity() {

        Map<AvailabilityZone, ResourceCapacity> azResourceCapacityMap = availabilityZones.stream()
                .collect(Collectors.toMap(availabilityZone -> availabilityZone, availabilityZone -> ResourceCapacity.builder().build(), (a, b) -> a));
        Map<AvailabilityZone, ResourceUsage> azCpuUsageMap = availabilityZones.stream()
                .collect(Collectors.toMap(availabilityZone -> availabilityZone, availabilityZone -> ResourceUsage.builder().build(), (a, b) -> a));

        Map<ClusterNodeType, ResourceCapacity> nodeTypeResourceCapacityMap = nodeTypes.stream()
                .collect(Collectors.toMap(nodeType -> nodeType, nodeType -> ResourceCapacity.builder().build(), (a, b) -> a));
        Map<ClusterNodeType, ResourceUsage> nodeTypeResourceUsageMap = nodeTypes.stream()
                .collect(Collectors.toMap(nodeType -> nodeType, nodeType -> ResourceUsage.builder().build(), (a, b) -> a));

        Map<ClusterNode, ResourceUsage> nodeResourceUsageMap = new HashMap<>(clusterNodes.size());

        clusterNodes.forEach(clusterNode -> {

            ResourceCapacity azResourceCapacity = azResourceCapacityMap.get(clusterNode.getAvailabilityZone());
            azResourceCapacity.setCpuCapacity(azResourceCapacity.getCpuCapacity() + clusterNode.getCpu());
            azResourceCapacity.setMemoryCapacity(azResourceCapacity.getMemoryCapacity() + clusterNode.getMemory());
            azResourceCapacity.setDiskCapacity(azResourceCapacity.getDiskCapacity() + clusterNode.getDisk());
            azResourceCapacityMap.put(clusterNode.getAvailabilityZone(), azResourceCapacity);

            ResourceCapacity nodeTypeResourceCapacity = nodeTypeResourceCapacityMap.get(clusterNode.getClusterNodeType());
            nodeTypeResourceCapacity.setCpuCapacity(nodeTypeResourceCapacity.getCpuCapacity() + clusterNode.getCpu());
            nodeTypeResourceCapacity.setMemoryCapacity(nodeTypeResourceCapacity.getMemoryCapacity() + clusterNode.getMemory());
            nodeTypeResourceCapacity.setDiskCapacity(nodeTypeResourceCapacity.getDiskCapacity() + clusterNode.getDisk());
            nodeTypeResourceCapacityMap.put(clusterNode.getClusterNodeType(), nodeTypeResourceCapacity);

            nodeResourceUsageMap.put(clusterNode, ResourceUsage.builder().build());

        });

        return ClusterUtilization.builder()
                .azResourceCapacityMap(azResourceCapacityMap)
                .azResourceUsageMap(azCpuUsageMap)
                .nodeTypeResourceCapacityMap(nodeTypeResourceCapacityMap)
                .nodeTypeResourceUsageMap(nodeTypeResourceUsageMap)
                .nodeResourceUsageMap(nodeResourceUsageMap)
                .build();
    }
}
