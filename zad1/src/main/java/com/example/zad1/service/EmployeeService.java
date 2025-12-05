package com.example.zad1.service;

import com.example.zad1.dto.EmployeeListView;
import com.example.zad1.model.CompanyStatistics;
import com.example.zad1.model.Employee;
import com.example.zad1.model.Position;
import com.example.zad1.model.EmploymentStatus;
import com.example.zad1.repository.DepartmentRepository;
import com.example.zad1.repository.EmployeeRepository;
import com.example.zad1.specification.EmployeeSpecification;

import java.util.*;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public EmployeeService(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public Page<EmployeeListView> getEmployeesList(Pageable pageable) {
        return employeeRepository.findAllProjectedBy(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Employee> searchEmployees(String company, Integer minSalary, Integer maxSalary, Pageable pageable) {
        return employeeRepository.findAll(EmployeeSpecification.filterBy(company, minSalary, maxSalary), pageable);
    }

    public Optional<Employee> getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    public List<Employee> getAllEmployees(){
        return employeeRepository.findAll();
    }

    @Transactional
    public boolean addEmployee(@Valid Employee employee){
        if (employee==null || employee.getEmail()==null || employee.getEmail().isBlank() || employeeRepository.existsByEmail(employee.getEmail())){
            return false;
        }
        if (employee.getDepartmentId()!=null){
            departmentRepository.findById(employee.getDepartmentId())
                    .ifPresent(employee::setDepartment);
        }
        employeeRepository.save(employee);
        return true;
    }

    @Transactional
    public Optional<Employee> updateEmployeeByEmail(String email, Employee changes) {
        if (email == null || changes == null) return Optional.empty();
        return employeeRepository.findByEmail(email).map(existing -> {
            existing.setFullName(changes.getFullName());
            existing.setCompanyName(changes.getCompanyName());
            existing.setPosition(changes.getPosition());
            existing.setSalary(changes.getSalary());
            existing.setStatus(changes.getStatus());
            existing.setDepartmentId(changes.getDepartmentId());

            departmentRepository.findById(existing.getDepartmentId())
                    .ifPresent(existing::setDepartment);

            return employeeRepository.save(existing);
        });
    }

    @Transactional
    public boolean deleteEmployeeByEmail(String email) {
        if (employeeRepository.existsByEmail(email)) {
            employeeRepository.deleteByEmail(email);
            return true;
        }
        return false;
    }

    @Transactional
    public Optional<Employee> updateStatusByEmail(String email, EmploymentStatus status){
        return employeeRepository.findByEmail(email).map(e -> {
            e.setStatus(status);
            return employeeRepository.save(e);
        });
    }

    @Transactional
    public void deleteAll() {
        employeeRepository.deleteAll();
    }

    @Transactional(readOnly = true)
    public Map<String, CompanyStatistics> getCompanyStatistics() {
        List<CompanyStatistics> statsList = employeeRepository.getCompanyStatisticsJPQL();

        return statsList.stream()
                .collect(Collectors.toMap(
                        CompanyStatistics::getCompanyName,
                        stat -> stat
                ));
    }

    @Transactional(readOnly = true)
    public List<Employee> getEmployeeByCompany(String company) {
        return employeeRepository.findAll(EmployeeSpecification.filterBy(company, null, null));
    }

    @Transactional(readOnly = true)
    public double getAverageSalary() {
        return employeeRepository.getAverageSalaryJPQL();
    }

    @Transactional(readOnly = true)
    public double getAverageSalaryByCompany(String company) {
        return employeeRepository.getAverageSalaryByCompanyJPQL(company);
    }

    public Optional<Employee> getHighestSalary() {
        return getAllEmployees().stream()
                .max(Comparator.comparingInt(Employee::getSalary));
    }

    public List<Employee> validateSalaryConsistency() {
        return employeeRepository.findAll().stream()
                .filter(Objects::nonNull)
                .filter(emp -> emp.getPosition() != null)
                .filter(emp -> emp.getSalary() < emp.getPosition().getSalary())
                .collect(Collectors.toList());
    }

    public Map<Position, List<Employee>> groupByPosition(){
        return getAllEmployees().stream()
                .collect(Collectors.groupingBy(Employee::getPosition));
    }

    @Transactional(readOnly = true)
    public Map<Position, Long> countByPosition() {
        List<Object[]> results = employeeRepository.countByPositionJPQL();
        Map<Position, Long> map = new LinkedHashMap<>();
        for (Object[] row : results) {
            map.put((Position) row[0], (Long) row[1]);
        }
        return map;
    }

    @Transactional(readOnly = true)
    public Map<EmploymentStatus, Long> countByStatus() {
        List<Object[]> results = employeeRepository.countByStatusJPQL();
        Map<EmploymentStatus, Long> map = new LinkedHashMap<>();
        for (Object[] row : results) {
            map.put((EmploymentStatus) row[0], (Long) row[1]);
        }
        return map;
    }

    @Transactional(readOnly = true)
    public List<Employee> getByStatus(EmploymentStatus status) {
        return employeeRepository.findByStatus(status);
    }

    public List<Employee> getAllAlphabetically(){
        return getAllEmployees().stream()
                .sorted(Comparator
                        .comparing(Employee::getLastName)
                        .thenComparing(Employee::getFirstName))
                .collect(Collectors.toList());
    }

    public void displayAll(){
        if (getAllEmployees().isEmpty()){
            System.out.println("No employees to display.");
            return;
        }
        getAllEmployees().forEach(System.out::println);
    }
}
