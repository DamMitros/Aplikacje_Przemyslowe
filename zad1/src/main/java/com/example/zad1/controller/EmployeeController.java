package com.example.zad1.controller;

import com.example.zad1.dto.EmployeeDTO;
import com.example.zad1.exception.DuplicateEmailException;
import com.example.zad1.exception.EmployeeNotFoundException;
import com.example.zad1.exception.InvalidDataException;
import com.example.zad1.model.Employee;
import com.example.zad1.model.EmploymentStatus;
import com.example.zad1.model.Position;
import com.example.zad1.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employees;

    public EmployeeController(EmployeeService employees) {
        this.employees = employees;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAll(@RequestParam(value = "company", required = false) String company) {
        List<Employee> list = (company == null || company.isBlank())
                ? employees.getAllEmployees()
                : employees.getEmployeeByCompany(company);
        return ResponseEntity.ok(toDtoList(list));
    }

    @GetMapping("/{email}")
    public ResponseEntity<EmployeeDTO> getByEmail(@PathVariable String email) {
        Employee emp = employees.getEmployeeByEmail(email)
                .orElseThrow(() -> new EmployeeNotFoundException(email));
        return ResponseEntity.ok(toDto(emp));
    }

    @PostMapping
    public ResponseEntity<EmployeeDTO> create(@Valid @RequestBody EmployeeDTO dto) {
        if (employees.getEmployeeByEmail(dto.getEmail()).isPresent()) {
            throw new DuplicateEmailException(dto.getEmail());
        }
        Employee entity = toEntity(dto);
        boolean added = employees.addEmployee(entity);
        if (!added) {
            throw new InvalidDataException("Employee data invalid");
        }
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{email}")
                .buildAndExpand(entity.getEmail())
                .toUri();
        return ResponseEntity.created(location).body(toDto(entity));
    }

    @PutMapping("/{email}")
    public ResponseEntity<EmployeeDTO> update(@PathVariable String email, @Valid @RequestBody EmployeeDTO dto) {
        if (dto.getEmail() != null && !email.equalsIgnoreCase(dto.getEmail())) {
            throw new InvalidDataException("Email in path and body must match");
        }
        employees.getEmployeeByEmail(email).orElseThrow(() -> new EmployeeNotFoundException(email));

        Employee changes = toEntity(dto);
        changes.setEmail(email);
        Employee updated = employees.updateEmployeeByEmail(email, changes)
                .orElseThrow(() -> new EmployeeNotFoundException(email));
        return ResponseEntity.ok(toDto(updated));
    }


    @DeleteMapping("/{email}")
    public ResponseEntity<Void> delete(@PathVariable String email) {
        boolean removed = employees.deleteEmployeeByEmail(email);
        if (!removed) throw new EmployeeNotFoundException(email);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{email}/status")
    public ResponseEntity<EmployeeDTO> updateStatus(@PathVariable String email, @RequestBody StatusUpdateDTO req) {
        if (req == null || req.status == null || req.status.isBlank()) {
            throw new InvalidDataException("Status is required");
        }
        EmploymentStatus status = EmploymentStatus.valueOf(req.status.toUpperCase());
        Employee updated = employees.updateStatusByEmail(email, status)
                .orElseThrow(() -> new EmployeeNotFoundException(email));
        return ResponseEntity.ok(toDto(updated));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<EmployeeDTO>> getByStatus(@PathVariable String status) {
        EmploymentStatus st = EmploymentStatus.valueOf(status.toUpperCase());
        List<Employee> list = employees.getByStatus(st);
        return ResponseEntity.ok(toDtoList(list));
    }

    private List<EmployeeDTO> toDtoList(List<Employee> list) {
        return list.stream().filter(Objects::nonNull).map(this::toDto).collect(Collectors.toList());
    }

    private EmployeeDTO toDto(Employee emp) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setFirstName(emp.getFirstName());
        dto.setLastName(emp.getLastName());
        dto.setEmail(emp.getEmail());
        dto.setCompany(emp.getCompanyName());
        dto.setPosition(emp.getPosition() != null ? emp.getPosition().name() : null);
        dto.setSalary(emp.getSalary());
        dto.setStatus(emp.getStatus() != null ? emp.getStatus().name() : null);
        return dto;
    }

    private Employee toEntity(EmployeeDTO dto) {
        String fullName = buildFullName(dto.getFirstName(), dto.getLastName());
        Position pos = dto.getPosition() != null
                ? Position.valueOf(dto.getPosition().toUpperCase()) : null;
        int sal = dto.getSalary() != null ? dto.getSalary() : 0;
        Employee e = new Employee(fullName, dto.getEmail(), dto.getCompany(), pos, sal);
        if (dto.getStatus() != null) {
            e.setStatus(EmploymentStatus.valueOf(dto.getStatus().toUpperCase()));
        }
        return e;
    }

    private String buildFullName(String first, String last) {
        String f = first == null ? "" : first.trim();
        String l = last == null ? "" : last.trim();
        return (f + " " + l).trim();
    }

    public static class StatusUpdateDTO {
        public String status;
    }
}
