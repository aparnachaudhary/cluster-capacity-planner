package io.github.aparnachaudhary.capacityplanner.extension;

import io.github.aparnachaudhary.capacityplanner.domain.CloudProcess;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.Comparator;

public class ProcessDifficultyComparator implements Comparator<CloudProcess> {

    @Override
    public int compare(CloudProcess o1, CloudProcess o2) {
        return new CompareToBuilder()
                .append(o1.getDifficultyIndex(), o2.getDifficultyIndex())
                .append(o1.getId(), o2.getId())
                .build();
    }
}
