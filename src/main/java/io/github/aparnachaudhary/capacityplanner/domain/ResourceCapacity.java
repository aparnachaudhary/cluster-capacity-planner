package io.github.aparnachaudhary.capacityplanner.domain;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ResourceCapacity {

    private int cpuCapacity;
    private int memoryCapacity;
    private int diskCapacity;


}
