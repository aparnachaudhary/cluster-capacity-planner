package io.github.aparnachaudhary.capacityplanner;

import io.github.aparnachaudhary.capacityplanner.domain.CloudBalance;
import io.github.aparnachaudhary.capacityplanner.domain.CloudComputer;
import io.github.aparnachaudhary.capacityplanner.domain.CloudProcess;
import io.github.aparnachaudhary.capacityplanner.listener.CloudBalanceSolverEventListener;
import io.github.aparnachaudhary.capacityplanner.repository.CloudComputerRepository;
import io.github.aparnachaudhary.capacityplanner.repository.CloudProcessRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class SolutionDataImporter implements ApplicationRunner {

    private CloudProcessRepository cloudProcessRepository;
    private CloudComputerRepository cloudComputerRepository;
    private CloudBalanceSolverEventListener solverEventListener;

    public SolutionDataImporter(CloudProcessRepository cloudProcessRepository, CloudComputerRepository cloudComputerRepository,
                                CloudBalanceSolverEventListener solverEventListener) {
        this.cloudProcessRepository = cloudProcessRepository;
        this.cloudComputerRepository = cloudComputerRepository;
        this.solverEventListener = solverEventListener;
    }

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {

        InputStream is = this.getClass().getResourceAsStream("/data/computer-value/computers-2.csv");
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(new InputStreamReader(is));
        List<CloudComputer> computers = new ArrayList<>(2);
        for (CSVRecord record : records) {
            computers.add(CloudComputer.builder()
                    .id(Long.parseLong(record.get("id")))
                    .cpuCapacity(Integer.parseInt(record.get("cpu")))
                    .memoryCapacity(Integer.parseInt(record.get("memory")))
                    .networkCapacity(Integer.parseInt(record.get("network")))
                    .cost(Integer.parseInt(record.get("cost")))
                    .nodeType(record.get("nodeType"))
                    .build());
        }
        cloudComputerRepository.saveAll(computers);

        is = this.getClass().getResourceAsStream("/data/process-value/processes-6.csv");
        records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(new InputStreamReader(is));
        List<CloudProcess> processes = new ArrayList<>(6);
        for (CSVRecord record : records) {
            processes.add(CloudProcess.builder()
                    .id(Long.parseLong(record.get("id")))
                    .cpuRequired(Integer.parseInt(record.get("cpu")))
                    .memoryRequired(Integer.parseInt(record.get("memory")))
                    .networkRequired(Integer.parseInt(record.get("network")))
                    .nodeTypeRequired(record.get("nodeType"))
                    .build());
        }
        cloudProcessRepository.saveAll(processes);

        val initSolution = new CloudBalance(0L, computers, processes);

        is = this.getClass().getResourceAsStream("/solution/solution.xml");
        SolverFactory<CloudBalance> solutionFactory = SolverFactory.createFromXmlInputStream(is);
        Solver<CloudBalance> solver = solutionFactory.buildSolver();
        solver.addEventListener(solverEventListener);

        log.info("Solving Capacity Planning Problem for initSolution={}", initSolution);
        CloudBalance solution = solver.solve(initSolution);
//        log.info("Solver score={}" + solver.explainBestScore());
        solution.getCloudProcesses().forEach(cloudProcess -> log.info(cloudProcess.toString()));

    }

}