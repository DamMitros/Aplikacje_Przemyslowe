package com.example.zad1.service;

import com.example.zad1.exception.FileNotFoundException;
import com.example.zad1.exception.FileStorageException;
import com.example.zad1.model.CompanyStatistics;
import com.example.zad1.model.Employee;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ReportGeneratorService {
    private final EmployeeService employeeService;
    private final FileStorageService storageService;

    public ReportGeneratorService(EmployeeService employeeService, FileStorageService storageService) {
        this.employeeService = employeeService;
        this.storageService = storageService;
    }

    public Resource generateCsv(String company) {
        List<Employee> list;
        if (company == null || company.isBlank()) {
            list = employeeService.getAllEmployees().stream()
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(Employee::getCompanyName, Comparator.nullsLast(String::compareToIgnoreCase))
                            .thenComparing(Employee::getLastName, Comparator.nullsLast(String::compareToIgnoreCase))
                            .thenComparing(Employee::getFirstName, Comparator.nullsLast(String::compareToIgnoreCase)))
                    .toList();
        } else {
            list = employeeService.getEmployeeByCompany(company).stream().filter(Objects::nonNull).toList();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("fullName,email,companyName,position,salary\n");
        for (Employee e : list) {
            if (e == null) continue;
            String fullName = safeCsv(e.getFullName());
            String email = safeCsv(e.getEmail());
            String companyName = safeCsv(e.getCompanyName());
            String position = e.getPosition() != null ? e.getPosition().name() : "";
            int salary = e.getSalary();
            sb.append(fullName).append(',')
                    .append(email).append(',')
                    .append(companyName).append(',')
                    .append(position).append(',')
                    .append(salary).append('\n');
        }

        String fileName = (company == null || company.isBlank()) ? "employees.csv" : "employees-" + company + ".csv";
        Path out = storageService.getReportsDir().resolve(fileName);
        storageService.ensureDir(out.getParent());
        try {
            Files.write(out, sb.toString().getBytes(StandardCharsets.UTF_8));
            return toResource(out);
        } catch (IOException e) {
            throw new FileStorageException("Nie udało się zapisać raportu CSV", e);
        }
    }

    public Resource generateCompanyStatisticsPdf(String companyName) {
        if (companyName == null || companyName.isBlank()) {
            throw new IllegalArgumentException("Nazwa firmy jest wymagana");
        }

        Map<String, CompanyStatistics> statsAll = employeeService.getCompanyStatistics();
        CompanyStatistics stats = statsAll.get(companyName);
        if (stats == null) {
            stats = statsAll.entrySet().stream()
                    .filter(e -> e.getKey() != null && e.getKey().equalsIgnoreCase(companyName))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
        }
        if (stats == null) {
            throw new FileNotFoundException("Brak danych dla firmy: " + companyName);
        }

        List<Employee> employees = employeeService.getEmployeeByCompany(companyName);
        String fileName = "statistics-" + companyName + ".pdf";
        Path out = storageService.getReportsDir().resolve(fileName);
        storageService.ensureDir(out.getParent());

        try (PdfWriter writer = new PdfWriter(out.toFile());
             PdfDocument pdf = new PdfDocument(writer);
             Document doc = new Document(pdf)) {

            doc.add(new Paragraph("Statystyki firmy: " + companyName).setBold().setFontSize(16));
            doc.add(new Paragraph("\nPodsumowanie:"));

            Table summary = new Table(new float[]{40, 60});
            summary.setWidth(100);

            addRow(summary, "Liczba pracowników", String.valueOf(stats.getEmployeeCount()));
            addRow(summary, "Średnie wynagrodzenie", String.format("%.2f", stats.getAverageSalary()));
            addRow(summary, "Najlepiej zarabiający", Objects.toString(stats.getTopEarnerFullName(), ""));
            addRow(summary, "Najwyższa pensja", String.valueOf(stats.getHighestSalary()));
            doc.add(summary);

            doc.add(new Paragraph("\nPracownicy:"));

            Table table = new Table(new float[]{35, 25, 20, 20});
            table.setWidth(100);

            addRow(table, "Imię i nazwisko", "Email", "Stanowisko", "Pensja");
            for (Employee e : employees) {
                if (e == null) continue;
                addRow(table,
                        Objects.toString(e.getFullName(), ""),
                        Objects.toString(e.getEmail(), ""),
                        e.getPosition() != null ? e.getPosition().name() : "",
                        String.valueOf(e.getSalary()));
            }
            doc.add(table);
        } catch (IOException ex) {
            throw new FileStorageException("Nie udało się wygenerować PDF", ex);
        }
        return toResource(out);
    }

    private void addRow(Table table, String c1, String c2) {
        table.addCell(c1);
        table.addCell(c2);
    }

    private void addRow(Table table, String c1, String c2, String c3, String c4) {
        table.addCell(c1);
        table.addCell(c2);
        table.addCell(c3);
        table.addCell(c4);
    }

    private String safeCsv(String value) {
        if (value == null) return "";
        String v = value.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\n") || v.contains("\r")) {
            return '"' + v + '"';
        }
        return v;
    }

    private Resource toResource(Path path) {
        try {
            return new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new FileStorageException("Błąd tworzenia zasobu dla pliku: " + path, e);
        }
    }
}
