package io.github.aparnachaudhary.capacityplanner.domain;

import lombok.val;
import org.junit.Test;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.test.impl.score.buildin.hardmediumsoft.HardMediumSoftScoreVerifier;

import java.util.Arrays;
import java.util.Collections;

public class ScoreConstraintTest {

    private HardMediumSoftScoreVerifier<CloudBalance> scoreVerifier = new HardMediumSoftScoreVerifier<>(SolverFactory.createFromXmlResource("solver/test-constraint-solver.xml"));

    @Test
    public void testCpuCapacity() {

        val c1 = CloudComputer.builder()
                .id(1L)
                .cpuCapacity(10)
                .memoryCapacity(100)
                .networkCapacity(1000)
                .cost(50)
                .build();

        val p1 = CloudProcess.builder()
                .id(1L)
                .cpuRequired(1)
                .cloudComputer(c1)
                .build();

        val p2 = CloudProcess.builder()
                .id(2L)
                .cpuRequired(10)
                .cloudComputer(c1)
                .build();

        val s1 = CloudBalance.builder()
                .id(1L)
                .cloudComputers(Collections.singletonList(c1))
                .cloudProcesses(Collections.singletonList(p1))
                .build();

        scoreVerifier.assertHardWeight("CPU capacity", 0, s1);

        s1.setCloudProcesses(Arrays.asList(p1, p2));
        scoreVerifier.assertHardWeight("CPU capacity", -1, s1);
    }

    @Test
    public void testMemoryCapacity() {

        val c1 = CloudComputer.builder()
                .id(1L)
                .cpuCapacity(10)
                .memoryCapacity(100)
                .networkCapacity(1000)
                .cost(50)
                .build();

        val p1 = CloudProcess.builder()
                .id(1L)
                .memoryRequired(100)
                .cloudComputer(c1)
                .build();

        val p2 = CloudProcess.builder()
                .id(2L)
                .memoryRequired(10)
                .cloudComputer(c1)
                .build();

        val s1 = CloudBalance.builder()
                .id(1L)
                .cloudComputers(Collections.singletonList(c1))
                .cloudProcesses(Collections.singletonList(p1))
                .availabilityZones(Collections.emptyList())
                .nodeTypes(Collections.emptyList())
                .build();

        scoreVerifier.assertHardWeight("Memory capacity", 0, s1);

        s1.setCloudProcesses(Arrays.asList(p1, p2));
        scoreVerifier.assertHardWeight("Memory capacity", -10, s1);
    }

    @Test
    public void testNetworkCapacity() {

        val c1 = CloudComputer.builder()
                .id(1L)
                .cpuCapacity(10)
                .memoryCapacity(100)
                .networkCapacity(1000)
                .cost(50)
                .build();

        val p1 = CloudProcess.builder()
                .id(1L)
                .networkRequired(500)
                .cloudComputer(c1)
                .build();

        val p2 = CloudProcess.builder()
                .id(2L)
                .networkRequired(1000)
                .cloudComputer(c1)
                .build();

        val s1 = CloudBalance.builder()
                .id(1L)
                .cloudComputers(Collections.singletonList(c1))
                .cloudProcesses(Collections.singletonList(p1))
                .build();

        scoreVerifier.assertHardWeight("Network capacity", 0, s1);

        s1.setCloudProcesses(Arrays.asList(p1, p2));
        scoreVerifier.assertHardWeight("Network capacity", -500, s1);
    }

    @Test
    public void testCost() {

        val c1 = CloudComputer.builder()
                .id(1L)
                .cpuCapacity(10)
                .memoryCapacity(100)
                .networkCapacity(1000)
                .cost(50)
                .build();

        val c2 = CloudComputer.builder()
                .id(2L)
                .cpuCapacity(10)
                .memoryCapacity(100)
                .networkCapacity(1000)
                .cost(500)
                .build();

        val p1 = CloudProcess.builder()
                .id(1L)
                .cpuRequired(1)
                .memoryRequired(100)
                .networkRequired(500)
                .cloudComputer(c1)
                .build();

        val p2 = CloudProcess.builder()
                .id(2L)
                .cpuRequired(10)
                .memoryRequired(10)
                .networkRequired(1000)
                .cloudComputer(c2)
                .build();

        val s1 = CloudBalance.builder()
                .id(1L)
                .cloudComputers(Arrays.asList(c1,c2))
                .cloudProcesses(Collections.singletonList(p1))
                .build();

        scoreVerifier.assertSoftWeight("Computer Cost", -50, s1);

        s1.setCloudProcesses(Arrays.asList(p1, p2));
        scoreVerifier.assertSoftWeight("Computer Cost", -550, s1);
    }

    @Test
    public void testNotAssigned() {

        val c1 = CloudComputer.builder()
                .id(1L)
                .cpuCapacity(10)
                .memoryCapacity(100)
                .networkCapacity(1000)
                .cost(50)
                .build();

        val p1 = CloudProcess.builder()
                .id(1L)
                .cpuRequired(1)
                .memoryRequired(100)
                .networkRequired(500)
                .build();

        val s1 = CloudBalance.builder()
                .id(1L)
                .cloudComputers(Collections.singletonList(c1))
                .cloudProcesses(Collections.singletonList(p1))
                .build();

        scoreVerifier.assertMediumWeight("Not Assigned", (-1) * p1.getDifficultyIndex(), s1);

        p1.setCloudComputer(c1);
        scoreVerifier.assertMediumWeight("Not Assigned", 0, s1);
    }
}
