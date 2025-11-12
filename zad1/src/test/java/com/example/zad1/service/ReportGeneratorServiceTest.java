package com.example.zad1.service;

import com.example.zad1.exception.FileNotFoundException;
import com.example.zad1.exception.FileStorageException;
import com.example.zad1.model.CompanyStatistics;
import com.example.zad1.model.Employee;
import com.example.zad1.model.Position;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReportGeneratorServiceTest {

    private Path uploads;
    private Path reports;

    private FileStorageService storageService;
    private EmployeeService employeeService;
    private ReportGeneratorService reportService;

    @BeforeEach
    void setUp() throws IOException {
        uploads = Files.createTempDirectory("uploads-rgs-");
        reports = Files.createTempDirectory("reports-rgs-");
        storageService = new FileStorageService(uploads.toString(), reports.toString());
        employeeService = mock(EmployeeService.class);
        reportService = new ReportGeneratorService(employeeService, storageService);
    }

    @AfterEach
    void tearDown() throws IOException {
        for (Path root : List.of(uploads, reports)) {
            if (root == null) continue;
            try (Stream<Path> walk = Files.walk(root)) {
                walk.sorted((a, b) -> Integer.compare(b.getNameCount(), a.getNameCount()))
                        .forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
            }
        }
    }

    @Test
    void generateCsv_allEmployees_sortedAndQuoted() throws Exception {
        Employee e1 = new Employee("Anna Kowalska", "anna@a", "Beta", Position.PROGRAMISTA, Position.PROGRAMISTA.getSalary());
        Employee e2 = new Employee("Jan Nowak", "jan@a", "alpha", Position.MANAGER, Position.MANAGER.getSalary());
        Employee e3 = new Employee("John, \"JJ\" Smith", "john@a", "Alpha", Position.PREZES, Position.PREZES.getSalary());
        Employee e4 = new Employee("Zoe Zee", "zoe@a", null, Position.STAZYSTA, Position.STAZYSTA.getSalary());

        when(employeeService.getAllEmployees()).thenReturn(List.of(e1, e2, e3, e4));

        Resource res = reportService.generateCsv(null);
        assertTrue(res.exists());
        String csv = Files.readString(Path.of(res.getURI()), StandardCharsets.UTF_8);

        String[] lines = csv.split("\r?\n");

        assertAll(
                ()->assertEquals("fullName,email,companyName,position,salary", lines[0]),
                ()->assertEquals(5, lines.length),
                ()->assertTrue(lines[1].toLowerCase(Locale.ROOT).startsWith("jan nowak,jan@a,alpha,manager,")),
                ()->assertTrue(lines[2].toLowerCase(Locale.ROOT).startsWith("\"john, \"\"jj\"\" smith\",john@a,alpha,prezes,")),
                ()->assertTrue(lines[3].toLowerCase(Locale.ROOT).startsWith("anna kowalska,anna@a,beta,programista,")),
                ()->assertTrue(lines[4].toLowerCase(Locale.ROOT).startsWith("zoe zee,zoe@a,,stazysta,"))
        );
    }

    @Test
    void generateCsv_specificCompany_onlyThatCompanyAndFileNameSuffix() throws Exception {
        String company = "Alpha";
        Employee e1 = new Employee("A A", "a@a", company, Position.STAZYSTA, 3000);
        Employee e2 = new Employee("B B", "b@a", company, Position.PROGRAMISTA, 8000);
        when(employeeService.getEmployeeByCompany(company)).thenReturn(List.of(e1, e2));

        Resource res = reportService.generateCsv(company);
        assertTrue(Objects.requireNonNull(res.getFilename()).endsWith("employees-" + company + ".csv"));
        String csv = Files.readString(Path.of(res.getURI()));
        String[] lines = csv.split("\r?\n");

        assertAll(
                ()->assertEquals("fullName,email,companyName,position,salary", lines[0]),
                ()->assertEquals(3, lines.length),
                ()->assertTrue(Arrays.stream(lines).anyMatch(l -> l.contains("A A,a@a,Alpha,STAZYSTA,3000"))),
                ()->assertTrue(Arrays.stream(lines).anyMatch(l -> l.contains("B B,b@a,Alpha,PROGRAMISTA,8000")))
        );
    }

    @Test
    void generateCsv_whenWriteFails_wrapsAsFileStorageException() {
        when(employeeService.getAllEmployees()).thenReturn(List.of());

        Path expected = storageService.getReportsDir().resolve("employees.csv");

        try (MockedStatic<Files> files = Mockito.mockStatic(Files.class)) {
            files.when(() -> Files.write(eq(expected), any(byte[].class))).thenThrow(new IOException("io"));
            files.when(() -> Files.createDirectories(any())).thenCallRealMethod();
            files.when(() -> Files.exists(any())).thenCallRealMethod();
            files.when(() -> Files.isReadable(any())).thenCallRealMethod();

            assertThrows(FileStorageException.class, () -> reportService.generateCsv(null));
        }
    }

    @Test
    void generateCsv_whenEnsureDirFails_wrapsAsFileStorageException() throws IOException {
        when(employeeService.getAllEmployees()).thenReturn(List.of(new Employee("X Y", "x@y", "C", Position.STAZYSTA, 1)));
        Path fileAsDir = Files.createTempFile("not-a-dir-", ".tmp");
        try {
            FileStorageService spy = Mockito.spy(storageService);
            Mockito.doReturn(fileAsDir).when(spy).getReportsDir();
            ReportGeneratorService svc = new ReportGeneratorService(employeeService, spy);
            assertThrows(FileStorageException.class, () -> svc.generateCsv(null));
        } finally {
            Files.deleteIfExists(fileAsDir);
        }
    }

    @Test
    void generateCompanyStatisticsPdf_nullOrBlank_throws() {
        assertAll(
                ()-> assertThrows(IllegalArgumentException.class, () -> reportService.generateCompanyStatisticsPdf(null)),
                ()-> assertThrows(IllegalArgumentException.class, () -> reportService.generateCompanyStatisticsPdf(" "))
        );
    }

    @Test
    void generateCompanyStatisticsPdf_noStatsForCompany_throwsFileNotFound() {
        when(employeeService.getCompanyStatistics()).thenReturn(Collections.emptyMap());
        assertThrows(FileNotFoundException.class, () -> reportService.generateCompanyStatisticsPdf("Alpha"));
    }

    @Test
    void generateCompanyStatisticsPdf_caseInsensitiveStats_writesValidPdf() throws Exception {
        String company = "Alpha";
        CompanyStatistics stats = new CompanyStatistics(2, 10000.0, "Top Earner", 12000);

        when(employeeService.getCompanyStatistics()).thenReturn(Map.of("alpha", stats));

        Employee e1 = new Employee("A A", "a@a", company, Position.STAZYSTA, 3000);
        Employee e2 = new Employee("B B", "b@a", company, Position.PROGRAMISTA, 8000);
        when(employeeService.getEmployeeByCompany(company)).thenReturn(List.of(e1, e2));

        Resource res = reportService.generateCompanyStatisticsPdf(company);
        assertNotNull(res);
        assertTrue(res.exists());
        assertTrue(Objects.requireNonNull(res.getFilename()).endsWith("statistics-" + company + ".pdf"));

        byte[] firstBytes;
        try (InputStream is = res.getInputStream()) {
            firstBytes = is.readNBytes(5);
        }

        assertAll(
                () -> assertEquals('%', firstBytes[0]),
                () -> assertEquals('P', firstBytes[1]),
                () -> assertEquals('D', firstBytes[2]),
                () -> assertEquals('F', firstBytes[3])
        );
    }

    @Test
    void generateCompanyStatisticsPdf_whenEnsureDirFails_wrapsAsFileStorageException() throws IOException {
        when(employeeService.getCompanyStatistics()).thenReturn(Map.of("Firm", new CompanyStatistics(1, 10.0, "A A", 10)));
        when(employeeService.getEmployeeByCompany("Firm")).thenReturn(List.of(new Employee("A A", "a@a", "Firm", Position.PROGRAMISTA, 10)));

        Path fileAsDir = Files.createTempFile("not-a-dir-", ".tmp");
        try {
            FileStorageService spy = Mockito.spy(storageService);
            Mockito.doReturn(fileAsDir).when(spy).getReportsDir();
            ReportGeneratorService svc = new ReportGeneratorService(employeeService, spy);
            assertThrows(FileStorageException.class, () -> svc.generateCompanyStatisticsPdf("Firm"));
        } finally {
            Files.deleteIfExists(fileAsDir);
        }
    }

    @Test
    void generateCsv_handlesNullEmployeeAndNullPosition() throws Exception {
        Employee e1 = new Employee("Anna Kowalska", "anna@a", "Beta", null, 5000);
        Employee e3 = new Employee("Jan Nowak", "jan@a", "Alpha", Position.MANAGER, 8000);

        when(employeeService.getAllEmployees()).thenReturn(Arrays.asList(e1, null, e3));

        Resource res = reportService.generateCsv(null);
        assertTrue(res.exists());

        String csv = Files.readString(Path.of(res.getURI()), StandardCharsets.UTF_8);
        String[] lines = csv.split("\r?\n");

        assertEquals(3, lines.length);
        assertTrue(Arrays.stream(lines).anyMatch(l -> l.contains("Anna Kowalska,anna@a,Beta,,5000")));
        assertTrue(Arrays.stream(lines).anyMatch(l -> l.contains("Jan Nowak,jan@a,Alpha,MANAGER,8000")));
    }

    @Test
    void generateCompanyStatisticsPdf_handlesNullEmployeeAndNullPosition() {
        String company = "Alpha";
        CompanyStatistics stats = new CompanyStatistics(2, 10000.0, "Top Earner", 12000);
        when(employeeService.getCompanyStatistics()).thenReturn(Map.of("Alpha", stats));

        Employee e1 = new Employee("A A", "a@a", company, null, 3000);
        Employee e3 = new Employee("B B", "b@a", company, Position.PROGRAMISTA, 8000);

        when(employeeService.getEmployeeByCompany(company))
                .thenReturn(Arrays.asList(e1, null, e3));

        Resource res = reportService.generateCompanyStatisticsPdf(company);
        assertNotNull(res);
        assertTrue(res.exists());
    }
}
