package com.example.zad1;

import com.example.zad1.exception.ApiException;
import com.example.zad1.model.CompanyStatistics;
import com.example.zad1.model.Department;
import com.example.zad1.model.Employee;
import com.example.zad1.service.ApiService;
import com.example.zad1.service.DepartmentService;
import com.example.zad1.service.EmployeeService;
import com.example.zad1.service.ImportService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class DataInitializer implements CommandLineRunner {

    private final EmployeeService employeeService;
    private final ImportService importService;
    private final ApiService apiService;
    private final DepartmentService departmentService;
    private final List<Employee> xmlEmployees;
    private final List<String> csvPaths;

    public DataInitializer(
            EmployeeService employeeService,
            ImportService importService,
            ApiService apiService,
            DepartmentService departmentService,
            @Qualifier("xmlEmployees") List<Employee> xmlEmployees,
            @Value("${app.import.csv-files}") String csvPathsProperty) {
        this.employeeService = employeeService;
        this.importService = importService;
        this.apiService = apiService;
        this.departmentService = departmentService;
        this.xmlEmployees = xmlEmployees;
        this.csvPaths = Arrays.stream(csvPathsProperty.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    @Override
    public void run(String... args) {
        for (String csvPath : csvPaths) {
            importService.importFromCsv(csvPath);
        }

        for (Employee e : xmlEmployees) {
            employeeService.addEmployee(e);
        }

        try {
            List<Employee> fromApi = apiService.fetchEmployeesFromApi();
            for (Employee e : fromApi) {
                employeeService.addEmployee(e);
            }
        } catch (ApiException ex) {
            System.out.println("Ostrzeżenie: " + ex.getMessage());
        }

        if (departmentService.getAllDepartments().isEmpty()) {
            Department it = new Department();
            it.setName("IT");
            it.setCompanyName("TechCorp");
            it.setLocation("Warszawa");
            it.setBudget(500_000);
            it.setManagerEmail("anna.nowak@techcorp.com");
            departmentService.addDepartment(it);

            Department hr = new Department();
            hr.setName("HR");
            hr.setCompanyName("TechCorp");
            hr.setLocation("Kraków");
            hr.setBudget(200_000);
            departmentService.addDepartment(hr);

            Department sales = new Department();
            sales.setName("Sprzedaż");
            sales.setCompanyName("TechCorp");
            sales.setLocation("Gdańsk");
            sales.setBudget(300_000);
            sales.setManagerEmail("karol.wisniewski@techcorp.com");
            departmentService.addDepartment(sales);
        }

        AtomicInteger techCorpIndex = new AtomicInteger(0);
        for (Employee employee : employeeService.getAllEmployees()) {
            if (!"TechCorp".equalsIgnoreCase(employee.getCompanyName())) {
                continue;
            }

            int idx = techCorpIndex.getAndIncrement() % 3;
            long deptId = switch (idx) {
                case 0 -> 1L;
                case 1 -> 2L;
                default -> 3L;
            };
            employee.setDepartmentId(deptId);
        }

        Map<String, CompanyStatistics> stats = employeeService.getCompanyStatistics();
        System.out.println("Statystyki firm:");
        stats.forEach((company, s) -> System.out.println(company + " -> " + s));

        List<Employee> inconsistencies = employeeService.validateSalaryConsistency();
        if (inconsistencies.isEmpty()) {
            System.out.println("Brak niezgodności wynagrodzeń.");
        } else {
            System.out.println("Pracownicy poniżej bazowej stawki:");
            inconsistencies.forEach(System.out::println);
        }

        System.out.println("Wszyscy pracownicy:");
        employeeService.displayAll();
    }
}