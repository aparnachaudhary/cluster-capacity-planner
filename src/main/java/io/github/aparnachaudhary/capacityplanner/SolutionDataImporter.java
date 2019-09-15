package io.github.aparnachaudhary.capacityplanner;

import io.github.aparnachaudhary.capacityplanner.domain.*;
import io.github.aparnachaudhary.capacityplanner.listener.ClusterBalanceSolverEventListener;
import io.github.aparnachaudhary.capacityplanner.repository.AvailabilityZoneRepository;
import io.github.aparnachaudhary.capacityplanner.repository.ClusterNodeRepository;
import io.github.aparnachaudhary.capacityplanner.repository.ClusterProcessRepository;
import io.github.aparnachaudhary.capacityplanner.repository.ClusterNodeTypeRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class SolutionDataImporter implements ApplicationRunner {

    private ClusterProcessRepository clusterProcessRepository;
    private ClusterNodeRepository clusterNodeRepository;
    private ClusterNodeTypeRepository clusterNodeTypeRepository;
    private AvailabilityZoneRepository availabilityZoneRepository;
    private ClusterBalanceSolverEventListener solverEventListener;

    public SolutionDataImporter(ClusterProcessRepository clusterProcessRepository, ClusterNodeRepository clusterNodeRepository,
                                ClusterNodeTypeRepository clusterNodeTypeRepository, AvailabilityZoneRepository availabilityZoneRepository,
                                ClusterBalanceSolverEventListener solverEventListener) {
        this.clusterProcessRepository = clusterProcessRepository;
        this.clusterNodeRepository = clusterNodeRepository;
        this.clusterNodeTypeRepository = clusterNodeTypeRepository;
        this.availabilityZoneRepository = availabilityZoneRepository;
        this.solverEventListener = solverEventListener;
    }

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {

        List<AvailabilityZone> availabilityZones = fetchAndSaveCloudAvailabilityZones();
        List<ClusterNodeType> nodeTypes = fetchAndSaveCloudNodeTypes();
        List<ClusterNode> clusterNodes = fetchAndSaveClusterNodes();
        List<ClusterProcess> processes = fetchAndSaveClusterProcesses();

        val initSolution = ClusterBalance.builder()
                .id(0L)
                .clusterNodes(clusterNodes)
                .clusterProcesses(processes)
                .availabilityZones(availabilityZones)
                .nodeTypes(nodeTypes)
                .build();

        InputStream cloudSolutionStream = this.getClass().getResourceAsStream("/solution/solution.xml");
        SolverFactory<ClusterBalance> solutionFactory = SolverFactory.createFromXmlInputStream(cloudSolutionStream);
        Solver<ClusterBalance> solver = solutionFactory.buildSolver();
        solver.addEventListener(solverEventListener);

        log.info("Solving Capacity Planning Problem for initSolution={}", initSolution);
        ClusterBalance solution = solver.solve(initSolution);
//        log.info("Solver score={}" + solver.explainBestScore());
        solution.getClusterProcesses().forEach(cloudProcess -> log.info(cloudProcess.toString()));

    }


    private List<ClusterProcess> fetchAndSaveClusterProcesses() throws IOException {

        InputStream is = this.getClass().getResourceAsStream("/data/processes/processes-small.csv");
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(new InputStreamReader(is));
        List<ClusterProcess> processes = new ArrayList<>(6);
        for (CSVRecord record : records) {
            processes.add(ClusterProcess.builder()
                    .id(Long.parseLong(record.get("id")))
                    .name(record.get("name"))
                    .cpuRequired(Integer.parseInt(record.get("cpu")))
                    .memoryRequired(Integer.parseInt(record.get("memory")))
                    .diskRequired(Integer.parseInt(record.get("disk")))
                    .availabilityZoneRequired(AvailabilityZone.builder().id(Long.parseLong(record.get("availabilityZone"))).build())
                    .clusterNodeType(ClusterNodeType.builder().id(Long.parseLong(record.get("clusterNodeType"))).build())
                    .build());
        }
        clusterProcessRepository.saveAll(processes);
        List<ClusterProcess> resultList = new ArrayList<>();
        clusterProcessRepository.findAll().iterator().forEachRemaining(resultList::add);
        return resultList;
    }

    private List<ClusterNode> fetchAndSaveClusterNodes() throws IOException {

        InputStream is = this.getClass().getResourceAsStream("/data/clusterNodes/clusterNodes-small.csv");
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(new InputStreamReader(is));
        List<ClusterNode> clusterNodes = new ArrayList<>(2);
        for (CSVRecord record : records) {
            clusterNodes.add(ClusterNode.builder()
                    .id(Long.parseLong(record.get("id")))
                    .name(record.get("name"))
                    .cpuCapacity(Integer.parseInt(record.get("cpu")))
                    .memoryCapacity(Integer.parseInt(record.get("memory")))
                    .diskCapacity(Integer.parseInt(record.get("disk")))
                    .availabilityZone(AvailabilityZone.builder().id(Long.parseLong(record.get("availabilityZone"))).build())
                    .clusterNodeType(ClusterNodeType.builder().id(Long.parseLong(record.get("clusterNodeType"))).build())
                    .cost(Integer.parseInt(record.get("cost")))
                    .build());
        }
        clusterNodeRepository.saveAll(clusterNodes);

        List<ClusterNode> resultList = new ArrayList<>();
        clusterNodeRepository.findAll()
                .iterator()
                .forEachRemaining(resultList::add);
        return resultList;
    }

    private List<ClusterNodeType> fetchAndSaveCloudNodeTypes() {

        List<ClusterNodeType> nodeTypes = new ArrayList<>(2);
        nodeTypes.add(ClusterNodeType.builder().id(0L).name("COMPUTE").build());
        nodeTypes.add(ClusterNodeType.builder().id(1L).name("EDGE").build());
        nodeTypes.add(ClusterNodeType.builder().id(2L).name("STORAGE").build());
        clusterNodeTypeRepository.saveAll(nodeTypes);
        List<ClusterNodeType> resultList = new ArrayList<>();
        clusterNodeTypeRepository.findAll().iterator().forEachRemaining(resultList::add);
        return resultList;
    }

    private List<AvailabilityZone> fetchAndSaveCloudAvailabilityZones() {

        List<AvailabilityZone> availabilityZones = new ArrayList<>(3);
        availabilityZones.add(AvailabilityZone.builder().id(0L).name("Zone1").build());
        availabilityZones.add(AvailabilityZone.builder().id(1L).name("Zone2").build());
        availabilityZones.add(AvailabilityZone.builder().id(2L).name("Zone3").build());
        availabilityZoneRepository.saveAll(availabilityZones);

        List<AvailabilityZone> resultList = new ArrayList<>();
        availabilityZoneRepository.findAll().iterator().forEachRemaining(resultList::add);
        return resultList;
    }
}
