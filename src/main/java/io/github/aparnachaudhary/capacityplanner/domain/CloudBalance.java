package io.github.aparnachaudhary.capacityplanner.domain;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Columns;
import org.hibernate.annotations.TypeDef;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.persistence.jpa.impl.score.buildin.hardmediumsoft.HardMediumSoftScoreHibernateType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@PlanningSolution
@Entity
@TypeDef(defaultForType = HardMediumSoftScore.class, typeClass = HardMediumSoftScoreHibernateType.class)
public class CloudBalance extends AbstractEntity {
    @Getter
    @Setter
    @PlanningScore
    @Columns(columns = {@Column(name="initScore"), @Column(name = "hardScore"), @Column(name = "mediumScore"), @Column(name = "softScore")})
    protected HardMediumSoftScore score;

    @Getter
    @Setter
    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "computerProvider")
    @OneToMany
    private List<CloudComputer> cloudComputers;

    @Getter
    @Setter
    @PlanningEntityCollectionProperty
    @OneToMany(mappedBy = "cloudComputer")
    private List<CloudProcess> cloudProcesses;

    public CloudBalance(Long id, List<CloudComputer> cloudComputers, List<CloudProcess> cloudProcesses) {
        super(id);
        this.cloudComputers = cloudComputers;
        this.cloudProcesses = cloudProcesses;
    }

    public CloudBalance() {
    }
}
