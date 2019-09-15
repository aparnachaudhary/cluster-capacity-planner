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
public class CloudBalance implements Serializable, Comparable<CloudBalance> {

    @PlanningId
    @Id
    protected Long id;

    @PlanningScore
    protected HardMediumSoftScore score;

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "computerProvider")
    private List<CloudComputer> cloudComputers;

    @PlanningEntityCollectionProperty
    private List<CloudProcess> cloudProcesses;
    private List<AvailabilityZone> availabilityZones;
    private List<NodeType> nodeTypes;


    @Override
    public int compareTo(CloudBalance o) {
        return new CompareToBuilder().append(getClass().getName(), o.getClass().getName())
                .append(id, o.id).toComparison();
    }

    @Override
    public String toString() {
        return getClass().getName().replaceAll(".*\\.", "") + "-" + id;
    }

    public CloudUtilization getResourceCapacity() {

        Map<AvailabilityZone, ResourceCapacity> azResourceCapacityMap = availabilityZones.stream()
                .collect(Collectors.toMap(availabilityZone -> availabilityZone, availabilityZone -> ResourceCapacity.builder().build(), (a, b) -> a));
        Map<AvailabilityZone, ResourceUsage> azCpuUsageMap = availabilityZones.stream()
                .collect(Collectors.toMap(availabilityZone -> availabilityZone, availabilityZone -> ResourceUsage.builder().build(), (a, b) -> a));

        Map<NodeType, ResourceCapacity> nodeTypeResourceCapacityMap = nodeTypes.stream()
                .collect(Collectors.toMap(nodeType -> nodeType, nodeType -> ResourceCapacity.builder().build(), (a, b) -> a));
        Map<NodeType, ResourceUsage> nodeTypeResourceUsageMap = nodeTypes.stream()
                .collect(Collectors.toMap(nodeType -> nodeType, nodeType -> ResourceUsage.builder().build(), (a, b) -> a));

        Map<CloudComputer, ResourceUsage> nodeResourceUsageMap = new HashMap<>(cloudComputers.size());

        cloudComputers.forEach(computer -> {

            ResourceCapacity azResourceCapacity = azResourceCapacityMap.get(computer.getAvailabilityZone());
            azResourceCapacity.setCpuCapacity(azResourceCapacity.getCpuCapacity() + computer.getCpuCapacity());
            azResourceCapacity.setMemoryCapacity(azResourceCapacity.getMemoryCapacity() + computer.getMemoryCapacity());
            azResourceCapacity.setDiskCapacity(azResourceCapacity.getDiskCapacity() + computer.getDiskCapacity());
            azResourceCapacityMap.put(computer.getAvailabilityZone(), azResourceCapacity);

            ResourceCapacity nodeTypeResourceCapacity = nodeTypeResourceCapacityMap.get(computer.getNodeType());
            nodeTypeResourceCapacity.setCpuCapacity(nodeTypeResourceCapacity.getCpuCapacity() + computer.getCpuCapacity());
            nodeTypeResourceCapacity.setMemoryCapacity(nodeTypeResourceCapacity.getMemoryCapacity() + computer.getMemoryCapacity());
            nodeTypeResourceCapacity.setDiskCapacity(nodeTypeResourceCapacity.getDiskCapacity() + computer.getDiskCapacity());
            nodeTypeResourceCapacityMap.put(computer.getNodeType(), nodeTypeResourceCapacity);

            nodeResourceUsageMap.put(computer, ResourceUsage.builder().build());

        });

        return CloudUtilization.builder()
                .azResourceCapacityMap(azResourceCapacityMap)
                .azResourceUsageMap(azCpuUsageMap)
                .nodeTypeResourceCapacityMap(nodeTypeResourceCapacityMap)
                .nodeTypeResourceUsageMap(nodeTypeResourceUsageMap)
                .nodeResourceUsageMap(nodeResourceUsageMap)
                .build();
    }
}
