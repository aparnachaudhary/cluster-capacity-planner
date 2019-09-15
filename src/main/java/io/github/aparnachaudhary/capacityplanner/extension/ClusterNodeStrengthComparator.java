package io.github.aparnachaudhary.capacityplanner.extension;

import io.github.aparnachaudhary.capacityplanner.domain.ClusterNode;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.Comparator;

public class ClusterNodeStrengthComparator implements Comparator<ClusterNode> {

    @Override
    public int compare(ClusterNode o1, ClusterNode o2) {
        return new CompareToBuilder()
                .append(o2.getDifficultyIndex(), o1.getDifficultyIndex())
                .append(o1.getId(), o2.getId())
                .build();
    }
}
