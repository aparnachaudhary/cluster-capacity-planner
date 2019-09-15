package io.github.aparnachaudhary.capacityplanner.domain;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class CloudUtilization {

    private Map<AvailabilityZone, ResourceCapacity> azResourceCapacityMap;
    private Map<AvailabilityZone, ResourceUsage> azResourceUsageMap;

    private Map<NodeType, ResourceCapacity> nodeTypeResourceCapacityMap;
    private Map<NodeType, ResourceUsage> nodeTypeResourceUsageMap;

    private Map<CloudComputer, ResourceUsage> nodeResourceUsageMap;

}
