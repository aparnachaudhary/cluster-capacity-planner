package io.github.aparnachaudhary.capacityplanner.domain;

import lombok.val;
import org.junit.Test;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.test.impl.score.buildin.hardmediumsoft.HardMediumSoftScoreVerifier;

import java.util.Arrays;
import java.util.Collections;

public class ScoreConstraintTest {

    private HardMediumSoftScoreVerifier<CloudBalance> scoreVerifier = new HardMediumSoftScoreVerifier<>(SolverFactory.createFromXmlResource("solution/solution.xml"));

    @Test
    public void testCpuCapacity() {
        val c1 = new CloudComputer(1L, 10, 100, 1000, 50);
        val p1 = new CloudProcess(1L, 1, 0, 0, c1);
        val p2 = new CloudProcess(2L, 10, 0, 0, c1);

        val s1 = new CloudBalance(1L, Collections.singletonList(c1), Collections.singletonList(p1));

        scoreVerifier.assertHardWeight("CPU capacity", 0, s1);

        s1.setCloudProcesses(Arrays.asList(p1, p2));
        scoreVerifier.assertHardWeight("CPU capacity", -1, s1);
    }

    @Test
    public void testMemoryCapacity() {
        val c1 = new CloudComputer(1L, 10, 100, 1000, 50);
        val p1 = new CloudProcess(1L, 1, 100, 0, c1);
        val p2 = new CloudProcess(2L, 10, 10, 0, c1);

        val s1 = new CloudBalance(1L, Collections.singletonList(c1), Collections.singletonList(p1));

        scoreVerifier.assertHardWeight("Memory capacity", 0, s1);

        s1.setCloudProcesses(Arrays.asList(p1, p2));
        scoreVerifier.assertHardWeight("Memory capacity", -10, s1);
    }

    @Test
    public void testNetworkCapacity() {
        val c1 = new CloudComputer(1L, 10, 100, 1000, 50);
        val p1 = new CloudProcess(1L, 1, 100, 500, c1);
        val p2 = new CloudProcess(2L, 10, 10, 1000, c1);

        val s1 = new CloudBalance(1L, Collections.singletonList(c1), Collections.singletonList(p1));

        scoreVerifier.assertHardWeight("Network capacity", 0, s1);

        s1.setCloudProcesses(Arrays.asList(p1, p2));
        scoreVerifier.assertHardWeight("Network capacity", -500, s1);
    }

    @Test
    public void testCost() {
        val c1 = new CloudComputer(1L, 10, 100, 1000, 50);
        val c2 = new CloudComputer(2L, 10, 100, 1000, 500);
        val p1 = new CloudProcess(1L, 1, 100, 500, c1);
        val p2 = new CloudProcess(2L, 10, 10, 1000, c2);

        val s1 = new CloudBalance(1L, Arrays.asList(c1, c2), Collections.singletonList(p1));

        scoreVerifier.assertSoftWeight("Computer Cost", -50, s1);

        s1.setCloudProcesses(Arrays.asList(p1, p2));
        scoreVerifier.assertSoftWeight("Computer Cost", -550, s1);
    }

    @Test
    public void testNotAssigned() {
        val c1 = new CloudComputer(1L, 10, 100, 1000, 50);
        val p1 = new CloudProcess(1L, 1, 100, 500);

        val s1 = new CloudBalance(1L, Collections.singletonList(c1), Collections.singletonList(p1));
        scoreVerifier.assertMediumWeight("Not Assigned", (-1) * p1.getDifficultyIndex(), s1);

        p1.setCloudComputer(c1);
        scoreVerifier.assertMediumWeight("Not Assigned", 0, s1);
    }
}
