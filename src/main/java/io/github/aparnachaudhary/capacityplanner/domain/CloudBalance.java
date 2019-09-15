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

    public ResourceCapacity getResourceCapacity() {

        Map<AvailabilityZone, Integer> azCpuCapacityMap = availabilityZones.stream()
                .collect(Collectors.toMap(availabilityZone -> availabilityZone, availabilityZone -> 0, (a, b) -> a));
        Map<AvailabilityZone, Integer> azCpuUsageMap = availabilityZones.stream()
                .collect(Collectors.toMap(availabilityZone -> availabilityZone, availabilityZone -> 0, (a, b) -> a));

        Map<NodeType, Integer> nodeTypeCpuCapacityMap = nodeTypes.stream()
                .collect(Collectors.toMap(nodeType -> nodeType, nodeType -> 0, (a, b) -> a));

        Map<NodeType, Integer> nodeTypeCpuUsageMap = nodeTypes.stream()
                .collect(Collectors.toMap(nodeType -> nodeType, nodeType -> 0, (a, b) -> a));

        Map<CloudComputer, Integer> cpuUsageMap = new HashMap<>(cloudComputers.size());

        cloudComputers.forEach(computer -> {
            int cpuCapacityNodeType = nodeTypeCpuCapacityMap.get(computer.getNodeType()) + computer.getCpuCapacity();
            nodeTypeCpuCapacityMap.put(computer.getNodeType(), cpuCapacityNodeType);
            int cpuCapacityAZ = azCpuCapacityMap.get(computer.getAvailabilityZone()) + computer.getCpuCapacity();
            azCpuCapacityMap.put(computer.getAvailabilityZone(), cpuCapacityAZ);
            cpuUsageMap.put(computer, 0);

        });

        return ResourceCapacity.builder()
                .azCpuCapacityMap(azCpuCapacityMap)
                .azCpuUsageMap(azCpuUsageMap)
                .nodeTypeCpuCapacityMap(nodeTypeCpuCapacityMap)
                .nodeTypeCpuUsageMap(nodeTypeCpuUsageMap)
                .cpuUsageMap(cpuUsageMap)
                .build();
    }
}
