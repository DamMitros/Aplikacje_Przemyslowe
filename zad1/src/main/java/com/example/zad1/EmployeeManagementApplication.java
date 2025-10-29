package com.example.zad1;

import com.example.zad1.exception.ApiException;
import com.example.zad1.model.CompanyStatistics;
import com.example.zad1.model.Employee;
import com.example.zad1.service.ApiService;
import com.example.zad1.service.EmployeeService;
import com.example.zad1.service.ImportService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

import java.util.List;
import java.util.Map;

@SpringBootApplication
@ImportResource("classpath:employees-beans.xml")
public class EmployeeManagementApplication implements CommandLineRunner {

    private final EmployeeService employeeService;
    private final ImportService importService;
    private final ApiService apiService;
    private final List<Employee> xmlEmployees;
    private final String csvPath;

    public EmployeeManagementApplication(
            EmployeeService employeeService,
            ImportService importService,
            ApiService apiService,
            @Qualifier("xmlEmployees") List<Employee> xmlEmployees,
            @Value("${app.import.csv-file}") String csvPath) {
        this.employeeService = employeeService;
        this.importService = importService;
        this.apiService = apiService;
        this.xmlEmployees = xmlEmployees;
        this.csvPath = csvPath;
    }

    public static void main(String[] args) {
        SpringApplication.run(EmployeeManagementApplication.class, args);
    }

    @Override
    public void run(String... args) {
        importService.importFromCsv(csvPath);

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
