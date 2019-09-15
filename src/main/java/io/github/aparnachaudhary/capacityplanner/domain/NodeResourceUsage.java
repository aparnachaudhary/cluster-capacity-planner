package io.github.aparnachaudhary.capacityplanner.domain;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class NodeResourceUsage {

    private int cpuUsage;
    private int memoryUsage;
    private int diskUsage;

}
