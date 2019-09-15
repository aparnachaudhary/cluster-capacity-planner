package io.github.aparnachaudhary.capacityplanner.domain;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class ClusterUtilization {

    private Map<AvailabilityZone, ResourceCapacity> azResourceCapacityMap;
    private Map<AvailabilityZone, ResourceUsage> azResourceUsageMap;

    private Map<ClusterNodeType, ResourceCapacity> nodeTypeResourceCapacityMap;
    private Map<ClusterNodeType, ResourceUsage> nodeTypeResourceUsageMap;

    private Map<ClusterNode, ResourceUsage> nodeResourceUsageMap;

}
