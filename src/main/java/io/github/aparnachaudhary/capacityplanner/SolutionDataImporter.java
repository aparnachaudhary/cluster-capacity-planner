package io.github.aparnachaudhary.capacityplanner;

import io.github.aparnachaudhary.capacityplanner.domain.*;
import io.github.aparnachaudhary.capacityplanner.listener.CloudBalanceSolverEventListener;
import io.github.aparnachaudhary.capacityplanner.repository.AvailabilityZoneRepository;
import io.github.aparnachaudhary.capacityplanner.repository.CloudComputerRepository;
import io.github.aparnachaudhary.capacityplanner.repository.CloudProcessRepository;
import io.github.aparnachaudhary.capacityplanner.repository.NodeTypeRepository;
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

    private CloudProcessRepository cloudProcessRepository;
    private CloudComputerRepository cloudComputerRepository;
    private NodeTypeRepository nodeTypeRepository;
    private AvailabilityZoneRepository availabilityZoneRepository;
    private CloudBalanceSolverEventListener solverEventListener;

    public SolutionDataImporter(CloudProcessRepository cloudProcessRepository, CloudComputerRepository cloudComputerRepository,
                                NodeTypeRepository nodeTypeRepository, AvailabilityZoneRepository availabilityZoneRepository,
                                CloudBalanceSolverEventListener solverEventListener) {
        this.cloudProcessRepository = cloudProcessRepository;
        this.cloudComputerRepository = cloudComputerRepository;
        this.nodeTypeRepository = nodeTypeRepository;
        this.availabilityZoneRepository = availabilityZoneRepository;
        this.solverEventListener = solverEventListener;
    }

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {

        List<AvailabilityZone> availabilityZones = fetchAndSaveCloudAvailabilityZones();
        List<NodeType> nodeTypes = fetchAndSaveCloudNodeTypes();
        List<CloudComputer> computers = fetchAndSaveCloudComputers();
        List<CloudProcess> processes = fetchAndSaveCloudProcesses();

        val initSolution = CloudBalance.builder()
                .id(0L)
                .cloudComputers(computers)
                .cloudProcesses(processes)
                .availabilityZones(availabilityZones)
                .nodeTypes(nodeTypes)
                .build();

        InputStream cloudSolutionStream = this.getClass().getResourceAsStream("/solution/solution.xml");
        SolverFactory<CloudBalance> solutionFactory = SolverFactory.createFromXmlInputStream(cloudSolutionStream);
        Solver<CloudBalance> solver = solutionFactory.buildSolver();
        solver.addEventListener(solverEventListener);

        log.info("Solving Capacity Planning Problem for initSolution={}", initSolution);
        CloudBalance solution = solver.solve(initSolution);
//        log.info("Solver score={}" + solver.explainBestScore());
        solution.getCloudProcesses().forEach(cloudProcess -> log.info(cloudProcess.toString()));

    }


    private List<CloudProcess> fetchAndSaveCloudProcesses() throws IOException {

        InputStream is = this.getClass().getResourceAsStream("/data/process-value/processes-6.csv");
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(new InputStreamReader(is));
        List<CloudProcess> processes = new ArrayList<>(6);
        for (CSVRecord record : records) {
            processes.add(CloudProcess.builder()
                    .id(Long.parseLong(record.get("id")))
                    .cpuRequired(Integer.parseInt(record.get("cpu")))
                    .memoryRequired(Integer.parseInt(record.get("memory")))
                    .diskRequired(Integer.parseInt(record.get("disk")))
                    .availabilityZoneRequired(availabilityZoneRepository.findById(Long.parseLong(record.get("availabilityZone"))).get())
                    .nodeTypeRequired(nodeTypeRepository.findById(Long.parseLong(record.get("nodeType"))).get())
                    .build());
        }
        cloudProcessRepository.saveAll(processes);
        List<CloudProcess> resultList = new ArrayList<>();
        cloudProcessRepository.findAll().iterator().forEachRemaining(resultList::add);
        return resultList;
    }

    private List<CloudComputer> fetchAndSaveCloudComputers() throws IOException {

        InputStream is = this.getClass().getResourceAsStream("/data/computer-value/computers-2.csv");
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(new InputStreamReader(is));
        List<CloudComputer> computers = new ArrayList<>(2);
        for (CSVRecord record : records) {
            computers.add(CloudComputer.builder()
                    .id(Long.parseLong(record.get("id")))
                    .cpuCapacity(Integer.parseInt(record.get("cpu")))
                    .memoryCapacity(Integer.parseInt(record.get("memory")))
                    .diskCapacity(Integer.parseInt(record.get("disk")))
                    .availabilityZone(availabilityZoneRepository.findById(Long.parseLong(record.get("availabilityZone"))).get())
                    .nodeType(nodeTypeRepository.findById(Long.parseLong(record.get("nodeType"))).get())
                    .cost(Integer.parseInt(record.get("cost")))
                    .build());
        }
        cloudComputerRepository.saveAll(computers);
        List<CloudComputer> resultList = new ArrayList<>();
        cloudComputerRepository.findAll().iterator().forEachRemaining(resultList::add);
        return resultList;
    }

    private List<NodeType> fetchAndSaveCloudNodeTypes() {

        List<NodeType> nodeTypes = new ArrayList<>(2);
        nodeTypes.add(NodeType.builder().id(0L).name("COMPUTE").build());
        nodeTypes.add(NodeType.builder().id(1L).name("EDGE").build());
        nodeTypes.add(NodeType.builder().id(2L).name("STORAGE").build());
        nodeTypeRepository.saveAll(nodeTypes);
        List<NodeType> resultList = new ArrayList<>();
        nodeTypeRepository.findAll().iterator().forEachRemaining(resultList::add);
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
