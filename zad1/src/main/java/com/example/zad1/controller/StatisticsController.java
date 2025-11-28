package com.example.zad1.controller;

import com.example.zad1.dto.CompanyStatisticsDTO;
import com.example.zad1.exception.InvalidDataException;
import com.example.zad1.model.CompanyStatistics;
import com.example.zad1.model.EmploymentStatus;
import com.example.zad1.model.Position;
import com.example.zad1.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final EmployeeService employees;

    public StatisticsController(EmployeeService employees) {
        this.employees = employees;
    }

    @GetMapping("/salary/average")
    public ResponseEntity<Map<String, Double>> averageSalary(@RequestParam(value = "company", required = false) String company) {
        double avg = (company == null || company.isBlank())
                ? employees.getAverageSalary()
                : employees.getAverageSalaryByCompany(company);
        Map<String, Double> body = new LinkedHashMap<>();
        body.put("averageSalary", avg);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/company/{companyName}")
    public ResponseEntity<CompanyStatisticsDTO> companyStats(@PathVariable String companyName) {
        Map<String, CompanyStatistics> stats = employees.getCompanyStatistics();
        CompanyStatistics cs = stats.get(companyName);
        if (cs == null) throw new InvalidDataException("No stats for company: " + companyName);
        CompanyStatisticsDTO dto = new CompanyStatisticsDTO(companyName, cs);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/positions")
    public ResponseEntity<Map<String, Long>> positions() {
        Map<Position, Long> counts = employees.countByPosition();
        Map<String, Long> out = new LinkedHashMap<>();
        counts.forEach((k, v) -> out.put(k != null ? k.name() : "UNKNOWN", v));
        return ResponseEntity.ok(out);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Long>> statusDistribution() {
        Map<EmploymentStatus, Long> counts = employees.countByStatus();
        Map<String, Long> out = new LinkedHashMap<>();
        counts.forEach((k, v) -> out.put(k != null ? k.name() : "UNKNOWN", v));
        return ResponseEntity.ok(out);
    }
}
