package com.example.zad1.service;

import com.example.zad1.model.CompanyStatistics;
import com.example.zad1.model.ImportSummary;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ImportServiceTest {

    @Test
    void shouldHandleWhenCSVisValid() {
        EmployeeService employeeService = new EmployeeService();
        ImportService importService = new ImportService(employeeService);

        String csvPath = Paths.get("data", "employees.csv").toString();
        ImportSummary sum = importService.importFromCsv(csvPath);

        Map<String, CompanyStatistics> stats = employeeService.getCompanyStatistics();
        CompanyStatistics techCorp = stats.get("TechCorp");

        assertAll(
                ()-> assertEquals(5, sum.getImportedCount()),
                ()-> assertTrue(sum.getErrors().size() >= 6),
                ()-> assertEquals(2, employeeService.validateSalaryConsistency().size()),
                ()-> assertEquals(4, stats.size()),
                ()-> {
                    assertNotNull(techCorp);
                    assertEquals(2, techCorp.getEmployeeCount());
                    assertEquals(19250.0, techCorp.getAverageSalary(), 0.001);
                    assertEquals("Jan Kowalski", techCorp.getTopEarnerFullName());
                }
        );
    }

    @Test
    void shouldHandleWhenCSVisEmpty() {
        EmployeeService employeeService = new EmployeeService();
        ImportService importService = new ImportService(employeeService);

        String csvPath = Paths.get("data", "empty.csv").toString();
        ImportSummary sum = importService.importFromCsv(csvPath);

        assertAll(
                ()-> assertEquals(0, sum.getImportedCount()),
                ()-> assertEquals(1, sum.getErrors().size()),
                ()-> assertEquals("Plik jest pusty", sum.getErrors().get(0))
        );
    }

    @Test
    void shouldHandleWhenCSVisNotExist() {
        EmployeeService employeeService = new EmployeeService();
        ImportService importService = new ImportService(employeeService);

        String csvPath = Paths.get("data", "nonexistent.csv").toString();
        ImportSummary sum = importService.importFromCsv(csvPath);

        assertAll(
                ()-> assertEquals(0, sum.getImportedCount()),
                ()-> assertEquals(1, sum.getErrors().size()),
                ()-> assertTrue(sum.getErrors().get(0).startsWith("Błąd podczas odczytu pliku:"))
        );
    }

    @Test
    void shouldHandleWhenXMLisValid() {
        EmployeeService employeeService = new EmployeeService();
        ImportService importService = new ImportService(employeeService);

        String xmlPath = Paths.get("data", "employees.xml").toString();
        ImportSummary sum = importService.importFromXml(xmlPath);
        Map<String, CompanyStatistics> stats = employeeService.getCompanyStatistics();
        CompanyStatistics techCorp = stats.get("TechCorp");

        assertAll(
                ()-> assertEquals(5, sum.getImportedCount()),
                ()-> assertTrue(sum.getErrors().size() >= 6),
                ()-> assertEquals(2, employeeService.validateSalaryConsistency().size()),
                ()-> assertEquals(4, stats.size()),
                ()-> {
                    assertNotNull(techCorp);
                    assertEquals(2, techCorp.getEmployeeCount());
                    assertEquals(19250.0, techCorp.getAverageSalary(), 0.001);
                    assertEquals("Jan Kowalski", techCorp.getTopEarnerFullName());
                }
        );
    }

    @Test
    void shouldHandleWhenXMLisNotExist() {
        EmployeeService employeeService = new EmployeeService();
        ImportService importService = new ImportService(employeeService);

        String xmlPath = Paths.get("data", "nonexistent.xml").toString();
        ImportSummary sum = importService.importFromXml(xmlPath);

        assertAll(
                ()-> assertEquals(0, sum.getImportedCount()),
                ()-> assertEquals(1, sum.getErrors().size()),
                ()-> assertTrue(sum.getErrors().get(0).startsWith("Błąd podczas odczytu pliku:"))
        );
    }

    @Test
    void shouldHandleWhenXMLisEmpty() {
        EmployeeService employeeService = new EmployeeService();
        ImportService importService = new ImportService(employeeService);

        String xmlPath = Paths.get("data", "empty.xml").toString();
        ImportSummary sum = importService.importFromXml(xmlPath);

        assertAll(
                () -> assertEquals(0, sum.getImportedCount()),
                () -> assertEquals(1, sum.getErrors().size()),
                () -> assertEquals("Plik jest pusty", sum.getErrors().get(0))
        );
    }
}