package io.github.aparnachaudhary.capacityplanner.domain;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class ResourceCapacity {

    private Map<AvailabilityZone, Integer> azCpuCapacityMap;
    private Map<AvailabilityZone, Integer> azCpuUsageMap;

    private Map<AvailabilityZone, Integer> azMemCapacityMap;
    private Map<AvailabilityZone, Integer> azMemUsageMap;

    private Map<AvailabilityZone, Integer> azDiskCapacityMap;
    private Map<AvailabilityZone, Integer> azDiskUsageMap;

    private Map<NodeType, Integer> nodeTypeCpuCapacityMap;
    private Map<NodeType, Integer> nodeTypeCpuUsageMap;

    private Map<NodeType, Integer> nodeTypeMemCapacityMap;
    private Map<NodeType, Integer> nodeTypeMemUsageMap;

    private Map<NodeType, Integer> nodeTypeDiskCapacityMap;
    private Map<NodeType, Integer> nodeTypeDiskUsageMap;

//    private Map<CloudComputer, Integer> cpuCapacityMap;
    private Map<CloudComputer, Integer> cpuUsageMap;


    public Integer getCpuCapacityByNodeType(NodeType nodeType) {
        return nodeTypeCpuCapacityMap.getOrDefault(nodeType, 0);
    }

    public Integer getMemCapacityByNodeType(NodeType nodeType) {
        return nodeTypeMemCapacityMap.getOrDefault(nodeType, 0);
    }

    public Integer getDiskCapacityByNodeType(NodeType nodeType) {
        return nodeTypeDiskCapacityMap.getOrDefault(nodeType, 0);
    }

    public Integer getCpuCapacityByAvailabilityZone(AvailabilityZone availabilityZone) {
        return azCpuCapacityMap.getOrDefault(availabilityZone, 0);
    }

    public Integer getMemCapacityByAvailabilityZone(AvailabilityZone availabilityZone) {
        return azMemCapacityMap.getOrDefault(availabilityZone, 0);
    }

    public Integer getDiskCapacityByAvailabilityZone(AvailabilityZone availabilityZone) {
        return azDiskCapacityMap.getOrDefault(availabilityZone, 0);
    }
}
