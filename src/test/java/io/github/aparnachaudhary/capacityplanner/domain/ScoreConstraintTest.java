package io.github.aparnachaudhary.capacityplanner.domain;

import lombok.val;
import org.junit.Test;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.test.impl.score.buildin.hardmediumsoft.HardMediumSoftScoreVerifier;

import java.util.Arrays;
import java.util.Collections;

public class ScoreConstraintTest {

    private HardMediumSoftScoreVerifier<ClusterBalance> scoreVerifier = new HardMediumSoftScoreVerifier<>(SolverFactory.createFromXmlResource("solver/test-constraint-solver.xml"));

    @Test
    public void testCpuCapacity() {

        val c1 = ClusterNode.builder()
                .id(1L)
                .cpuCapacity(10)
                .memoryCapacity(100)
                .diskCapacity(1000)
                .cost(50)
                .build();

        val p1 = ClusterProcess.builder()
                .id(1L)
                .cpuRequired(1)
                .clusterNode(c1)
                .build();

        val p2 = ClusterProcess.builder()
                .id(2L)
                .cpuRequired(10)
                .clusterNode(c1)
                .build();

        val s1 = ClusterBalance.builder()
                .id(1L)
                .clusterNodes(Collections.singletonList(c1))
                .clusterProcesses(Collections.singletonList(p1))
                .build();

        scoreVerifier.assertHardWeight("CPU capacity", 0, s1);

        s1.setClusterProcesses(Arrays.asList(p1, p2));
        scoreVerifier.assertHardWeight("CPU capacity", -1, s1);
    }

    @Test
    public void testMemoryCapacity() {

        val c1 = ClusterNode.builder()
                .id(1L)
                .cpuCapacity(10)
                .memoryCapacity(100)
                .diskCapacity(1000)
                .cost(50)
                .build();

        val p1 = ClusterProcess.builder()
                .id(1L)
                .memoryRequired(100)
                .clusterNode(c1)
                .build();

        val p2 = ClusterProcess.builder()
                .id(2L)
                .memoryRequired(10)
                .clusterNode(c1)
                .build();

        val s1 = ClusterBalance.builder()
                .id(1L)
                .clusterNodes(Collections.singletonList(c1))
                .clusterProcesses(Collections.singletonList(p1))
                .availabilityZones(Collections.emptyList())
                .nodeTypes(Collections.emptyList())
                .build();

        scoreVerifier.assertHardWeight("Memory capacity", 0, s1);

        s1.setClusterProcesses(Arrays.asList(p1, p2));
        scoreVerifier.assertHardWeight("Memory capacity", -10, s1);
    }

    @Test
    public void testDiskCapacity() {

        val c1 = ClusterNode.builder()
                .id(1L)
                .cpuCapacity(10)
                .memoryCapacity(100)
                .diskCapacity(1000)
                .cost(50)
                .build();

        val p1 = ClusterProcess.builder()
                .id(1L)
                .diskRequired(500)
                .clusterNode(c1)
                .build();

        val p2 = ClusterProcess.builder()
                .id(2L)
                .diskRequired(1000)
                .clusterNode(c1)
                .build();

        val s1 = ClusterBalance.builder()
                .id(1L)
                .clusterNodes(Collections.singletonList(c1))
                .clusterProcesses(Collections.singletonList(p1))
                .build();

        scoreVerifier.assertHardWeight("Disk capacity", 0, s1);

        s1.setClusterProcesses(Arrays.asList(p1, p2));
        scoreVerifier.assertHardWeight("Disk capacity", -500, s1);
    }

    @Test
    public void testCost() {

        val c1 = ClusterNode.builder()
                .id(1L)
                .cpuCapacity(10)
                .memoryCapacity(100)
                .diskCapacity(1000)
                .cost(50)
                .build();

        val c2 = ClusterNode.builder()
                .id(2L)
                .cpuCapacity(10)
                .memoryCapacity(100)
                .diskCapacity(1000)
                .cost(500)
                .build();

        val p1 = ClusterProcess.builder()
                .id(1L)
                .cpuRequired(1)
                .memoryRequired(100)
                .diskRequired(500)
                .clusterNode(c1)
                .build();

        val p2 = ClusterProcess.builder()
                .id(2L)
                .cpuRequired(10)
                .memoryRequired(10)
                .diskRequired(1000)
                .clusterNode(c2)
                .build();

        val s1 = ClusterBalance.builder()
                .id(1L)
                .clusterNodes(Arrays.asList(c1,c2))
                .clusterProcesses(Collections.singletonList(p1))
                .build();

        scoreVerifier.assertSoftWeight("ClusterNode Cost", -50, s1);

        s1.setClusterProcesses(Arrays.asList(p1, p2));
        scoreVerifier.assertSoftWeight("ClusterNode Cost", -550, s1);
    }

    @Test
    public void testNotAssigned() {

        val c1 = ClusterNode.builder()
                .id(1L)
                .cpuCapacity(10)
                .memoryCapacity(100)
                .diskCapacity(1000)
                .cost(50)
                .build();

        val p1 = ClusterProcess.builder()
                .id(1L)
                .cpuRequired(1)
                .memoryRequired(100)
                .diskRequired(500)
                .build();

        val s1 = ClusterBalance.builder()
                .id(1L)
                .clusterNodes(Collections.singletonList(c1))
                .clusterProcesses(Collections.singletonList(p1))
                .build();

        scoreVerifier.assertMediumWeight("Not Assigned", (-1) * p1.getDifficultyIndex(), s1);

        p1.setClusterNode(c1);
        scoreVerifier.assertMediumWeight("Not Assigned", 0, s1);
    }
}
