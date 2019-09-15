package io.github.aparnachaudhary.capacityplanner.extension;

import io.github.aparnachaudhary.capacityplanner.domain.ClusterProcess;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.Comparator;

public class ProcessDifficultyComparator implements Comparator<ClusterProcess> {

    @Override
    public int compare(ClusterProcess o1, ClusterProcess o2) {
        return new CompareToBuilder()
                .append(o1.getDifficultyIndex(), o2.getDifficultyIndex())
                .append(o1.getId(), o2.getId())
                .build();
    }
}
