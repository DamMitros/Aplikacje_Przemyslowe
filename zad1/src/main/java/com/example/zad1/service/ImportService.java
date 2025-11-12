package com.example.zad1.service;

import com.example.zad1.exception.InvalidDataException;
import com.example.zad1.model.ImportSummary;
import com.example.zad1.model.Position;
import com.example.zad1.model.Employee;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Service
public class ImportService {
    private final EmployeeService employeeService;
    private final Path uploadDir;

    public ImportService(EmployeeService employeeService) {
        this.employeeService = employeeService;
        this.uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
    }

    public ImportSummary importFromCsv(String filePath) {
        List<String> errors = new ArrayList<>();
        int imported = 0;

        Path path = resolvePath(filePath);
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;

            line = br.readLine();
            lineNumber++;
            if (line == null) {
                return new ImportSummary(0, List.of("Plik jest pusty"));
            }

            while ((line = br.readLine()) != null){
                lineNumber++;
                if (line.trim().isBlank()) continue;

                try {
                    String[] parts = line.split(",");
                    if (parts.length < 6){
                        throw new InvalidDataException("Linia " + lineNumber + ": za mało pól");
                    }

                    String firstName = parts[0].trim();
                    String lastName = parts[1].trim();
                    String email = parts[2].trim();
                    String companyName = parts[3].trim();
                    String positionStr = parts[4].trim();
                    String salaryStr = parts[5].trim();

                    if (addingEmp(firstName, lastName, email, companyName, positionStr, salaryStr, lineNumber)) {;
                        imported++;
                    } else {
                        throw new InvalidDataException("Nie udało się dodać pracownika z linii " + lineNumber + ". Możliwe, że email już istnieje.");
                    }
                } catch (InvalidDataException e) {
                    errors.add(e.getMessage());
                }
            }
        } catch (Exception e) {
            errors.add("Błąd podczas odczytu pliku: " + e.getMessage());
        }
        return new ImportSummary(imported, errors);
    }

    public ImportSummary importFromXml(String filePath) {
        List<String> errors = new ArrayList<>();
        int imported = 0;

        Path path = resolvePath(filePath);
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            if (lines.isEmpty()) {
                return new ImportSummary(0, List.of("Plik jest pusty"));
            }

            int lineNumber = 0;
            Map<String, String> data = new HashMap<>();

            for (String line : lines) {
                lineNumber++;
                line = line.trim();
                if (line.isBlank()) continue;

                if (line.startsWith("<employee>")) {
                    data.clear();
                } else if (line.startsWith("</employee>")){
                    try{
                        String firstName = data.get("firstName");
                        String lastName = data.get("lastName");
                        String email = data.get("email");
                        String companyName = data.get("companyName");
                        String positionStr = data.get("position");
                        String salaryStr = data.get("salary");

                        if (firstName==null || lastName==null || email==null || positionStr==null || salaryStr==null) {
                            throw new InvalidDataException("Linia " + lineNumber + ": Brak wymaganych pól.");
                        }

                        if (addingEmp(firstName, lastName, email, companyName, positionStr, salaryStr, lineNumber)) {;
                            imported++;
                        } else {
                            throw new InvalidDataException("Nie udało się dodać pracownika z linii " + lineNumber + ". Możliwe, że email już istnieje.");
                        }
                    } catch (InvalidDataException e) {
                        errors.add(e.getMessage());
                    }
                } else {
                    if (line.contains("<") && line.contains(">") && line.contains("</")) {
                        int start = line.indexOf("<") + 1;
                        int end = line.indexOf(">");
                        String key = line.substring(start, end).trim();
                        int closeStart = line.indexOf("</" + key + ">");
                        String value = line.substring(end + 1, closeStart).trim();

                        data.put(key, value);
                    }
                }

            }

        } catch (Exception e) {
            errors.add("Błąd podczas odczytu pliku: " + e.getMessage());
        }

        return new ImportSummary(imported, errors);
    }

    private Path resolvePath(String filePath){
        Path p = Paths.get(filePath);
        if (!Files.exists(p)) {
            p = uploadDir.resolve(filePath).normalize();
        }
        return p;
    }

    private boolean addingEmp(String firstName, String lastName, String email, String companyName, String positionStr, String salaryStr, int lineNumber) {
        if (!validatePosition(String.valueOf(positionStr))) {
            throw new InvalidDataException("Stanowisko w linii " + lineNumber + " nie istnieje w bazie danych.");
        }

        int salary;
        try {
            salary = Integer.parseInt(salaryStr);
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Wynagrodzenie w linii " + lineNumber + " nie jest liczbą.", e);
        }
        if (!(salary > 0)) {
            throw new InvalidDataException("Wynagrodzenie w linii " + lineNumber + " jest ujemne.");
        }

        Employee emp = new Employee(firstName + " " + lastName, email, companyName, Position.valueOf(positionStr.toUpperCase()), salary);
        return employeeService.addEmployee(emp);
    }

    private boolean validatePosition(String positionStr) {
        for (Position pos : Position.values()) {
            if (pos.name().equalsIgnoreCase(positionStr)) {
                return true;
            }
        }
        return false;
    }
}
