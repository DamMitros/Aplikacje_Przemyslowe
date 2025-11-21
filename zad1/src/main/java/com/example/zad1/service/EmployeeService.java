package com.example.zad1.service;

import com.example.zad1.dao.EmployeeDAO;
import com.example.zad1.model.CompanyStatistics;
import com.example.zad1.model.Employee;
import com.example.zad1.model.Position;
import com.example.zad1.model.EmploymentStatus;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {
    private final EmployeeDAO employeeDAO;

    public EmployeeService(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }

    @Transactional
    public boolean addEmployee(Employee employee){
        if (employee==null || employee.getEmail()==null || employee.getEmail().isBlank())
            return false;
        if (employeeDAO.findByEmail(employee.getEmail()).isPresent()) {
            return false;
        }
        employeeDAO.save(employee);
        return true;
    }

    public List<Employee> getAllEmployees(){
        return employeeDAO.findAll();
    }

    public Optional<Employee> getEmployeeByEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        return employeeDAO.findByEmail(email);
    }

    public Optional<Employee> UpdateEmployeeByEmail(String email, Employee changes) {
        Optional<Employee> existing = employeeDAO.findByEmail(email);
        if (existing.isPresent()) {
            Employee emp = existing.get();
            emp.setFullName(changes.getFullName());
            emp.setCompanyName(changes.getCompanyName());
            emp.setPosition(changes.getPosition());
            emp.setSalary(changes.getSalary());
            emp.setStatus(changes.getStatus());
            emp.setDepartmentId(changes.getDepartmentId());

            employeeDAO.save(emp);
            return Optional.of(emp);
        }
        return Optional.empty();
    }

    @Transactional
    public Optional<Employee> updateStatusByEmail(String email, EmploymentStatus status){
        Optional<Employee> existing = employeeDAO.findByEmail(email);
        if (existing.isPresent()) {
            Employee emp = existing.get();
            emp.setStatus(status);
            employeeDAO.save(emp);
            return Optional.of(emp);
        }
        return Optional.empty();
    }

    @Transactional
    public boolean deleteEmployeeByEmail(String email) {
        return employeeDAO.delete(email);
    }

    @Transactional(readOnly = true)
    public Map<String, CompanyStatistics> getCompanyStatistics() {
        List<CompanyStatistics> statsList = employeeDAO.getCompanyStatistics();

        return statsList.stream()
                .collect(java.util.stream.Collectors.toMap(
                        CompanyStatistics::getCompanyName,
                        stat -> stat
                ));
    }

    public List<Employee> getEmployeeByCompany(String companyName){
        if (companyName==null) return Collections.emptyList();
        return getAllEmployees().stream()
                .filter(emp -> companyName.equalsIgnoreCase(emp.getCompanyName()))
                .collect(Collectors.toList());
    }

    public double getAverageSalary() {
        return getAllEmployees().stream()
                .mapToInt(Employee::getSalary)
                .average()
                .orElse(0.0);
    }

    public double getAverageSalaryByCompany(String companyName) {
        return getEmployeeByCompany(companyName).stream()
                .mapToInt(Employee::getSalary)
                .average().orElse(0.0);
    }

    public Optional<Employee> getHighestSalary() {
        return getAllEmployees().stream()
                .max(Comparator.comparingInt(Employee::getSalary));
    }

    public List<Employee> validateSalaryConsistency(){
        return getAllEmployees().stream()
                .filter(Objects::nonNull)
                .filter(emp -> emp.getPosition() != null)
                .filter(emp -> emp.getSalary() < emp.getPosition().getSalary())
                .collect(Collectors.toList());
    }

    public Map<Position, List<Employee>> groupByPosition(){
        return getAllEmployees().stream()
                .collect(Collectors.groupingBy(Employee::getPosition));
    }

    public Map<Position, Integer> countByPosition(){
        return getAllEmployees().stream()
                .collect(Collectors.groupingBy(
                        Employee::getPosition,
                        Collectors.summingInt(e -> 1)
                ));
    }

    public Map<EmploymentStatus, Integer> countByStatus() {
        return getAllEmployees().stream()
                .collect(Collectors.groupingBy(
                        Employee::getStatus,
                        Collectors.summingInt(e -> 1)
                ));
    }

    public List<Employee> getByStatus(EmploymentStatus status) {
        if (status == null) return Collections.emptyList();
        return getAllEmployees().stream()
                .filter(e -> e.getStatus() == status)
                .collect(Collectors.toList());
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

    @Transactional
    public void deleteAll() {
        employeeDAO.deleteAll();
    }
}
