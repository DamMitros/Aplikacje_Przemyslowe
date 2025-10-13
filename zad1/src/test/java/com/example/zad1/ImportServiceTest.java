package com.example.zad1;

import com.example.zad1.model.CompanyStatistics;
import com.example.zad1.model.ImportSummary;
import com.example.zad1.service.EmployeeService;
import com.example.zad1.service.ImportService;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ImportServiceTest {

    @Test
    void testImportFromCsv_SummaryAndAnalytics() {
        EmployeeService employeeService = new EmployeeService();
        ImportService importService = new ImportService(employeeService);

        String csvPath = Paths.get("data", "employees.csv").toString();
        ImportSummary sum = importService.importFromCsv(csvPath);

        assertEquals(5, sum.getImportedCount());
        assertTrue(sum.getErrors().size() >= 6);

        assertEquals(2, employeeService.validateSalaryConsistency().size());

        Map<String, CompanyStatistics> stats = employeeService.getCompanyStatistics();
        assertEquals(4, stats.size());

        CompanyStatistics techCorp = stats.get("TechCorp");
        assertNotNull(techCorp);
        assertEquals(2, techCorp.getEmployeeCount());
        assertEquals(19250.0, techCorp.getAverageSalary(), 0.001);
        assertEquals("Jan Kowalski", techCorp.getTopEarnerFullName());
    }
}