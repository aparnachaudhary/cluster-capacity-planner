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

    private Map<CloudComputer, Integer> cpuCapacityMap;

    private Map<CloudComputer, NodeResourceUsage> nodeUsageMap;

}
