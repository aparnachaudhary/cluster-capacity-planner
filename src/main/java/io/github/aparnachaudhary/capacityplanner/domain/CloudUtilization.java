package io.github.aparnachaudhary.capacityplanner.domain;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class CloudUtilization {

    private Map<AvailabilityZone, ResourceCapacity> azResourceCapacityMap;
    private Map<AvailabilityZone, ResourceUsage> azResourceUsageMap;

    private Map<NodeType, Integer> nodeTypeCpuCapacityMap;
    private Map<NodeType, Integer> nodeTypeCpuUsageMap;

    private Map<CloudComputer, ResourceUsage> nodeUsageMap;

}
