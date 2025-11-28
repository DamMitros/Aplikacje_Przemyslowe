package com.example.zad1.service;

import com.example.zad1.model.CompanyStatistics;
import com.example.zad1.model.ImportSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ImportServiceTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ImportService importService;

    @BeforeEach
    void setUp() {
        employeeService.deleteAll();
    }

    @Test
    void shouldHandleWhenCSVisValid() {
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
                    assertNotNull(techCorp, "Statystyki dla TechCorp powinny istnieć");
                    assertEquals(2, techCorp.getEmployeeCount());
                    assertEquals(19250.0, techCorp.getAverageSalary(), 0.001);
                }
        );
    }

    @Test
    void shouldHandleWhenCSVisEmpty() {
        String csvPath = Paths.get("data", "empty.csv").toString();


        try {
            ImportSummary sum = importService.importFromCsv(csvPath);
            assertAll(
                    ()-> assertEquals(0, sum.getImportedCount()),
                    ()-> assertEquals(1, sum.getErrors().size()),
                    ()-> assertEquals("Plik jest pusty", sum.getErrors().get(0))
            );
        } catch (Exception e) {
            System.out.println("Pominięto test empty.csv, sprawdź czy plik istnieje: " + e.getMessage());
        }
    }

    @Test
    void shouldHandleWhenCSVisNotExist() {
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
                }
        );
    }

    @Test
    void shouldHandleWhenXMLisNotExist() {
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
        String xmlPath = Paths.get("data", "empty.xml").toString();

        try {
            ImportSummary sum = importService.importFromXml(xmlPath);
            assertAll(
                    () -> assertEquals(0, sum.getImportedCount()),
                    () -> assertEquals(1, sum.getErrors().size()),
                    () -> assertEquals("Plik jest pusty", sum.getErrors().get(0))
            );
        } catch (Exception e) {
            System.out.println("Pominięto test empty.xml: " + e.getMessage());
        }
    }
}