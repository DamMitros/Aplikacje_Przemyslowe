package com.example.zad1.service;

import com.example.zad1.exception.InvalidDataException;
import com.example.zad1.model.ImportSummary;
import com.example.zad1.model.Position;
import com.example.zad1.model.Employee;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ImportService {
    private final EmployeeService employeeService;

    public ImportService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    public ImportSummary importFromCsv(String filePath) {
        List<String> errors = new ArrayList<>();
        int imported = 0;

        Path path = Paths.get(filePath);
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

                    if (!validatePosition(positionStr)) {
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
                    boolean added = employeeService.addEmployee(emp);
                    if (added) {
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

    private boolean validatePosition(String positionStr) {
        for (Position pos : Position.values()) {
            if (pos.name().equalsIgnoreCase(positionStr)) {
                return true;
            }
        }
        return false;
    }
}
