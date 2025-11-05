package com.example.zad1.service;

import com.example.zad1.model.CompanyStatistics;
import com.example.zad1.model.Employee;
import com.example.zad1.model.Position;
import com.example.zad1.model.EmploymentStatus;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {
    private final Set<Employee> employees = new HashSet<>();

    public boolean addEmployee(Employee employee) {
        if (employee==null || employee.getEmail()==null || employee.getEmail().isBlank())
            return false;
        return employees.add(employee);
    }
    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employees);
    }
    public Optional<Employee> GetEmployeeByEmail(String email){
        if (email == null || email.isBlank()) return Optional.empty();
        return employees.stream().filter(emp -> email.equalsIgnoreCase(emp.getEmail())).findFirst();
    }
    public Optional<Employee> UpdateEmployeeByEmail(String email, Employee changes) {
        if (email == null || email.isBlank() || changes == null) return Optional.empty();
        Optional<Employee> empOpt = GetEmployeeByEmail(email);
        empOpt.ifPresent(emp -> {
            emp.setFullName(changes.getFullName());
            emp.setCompanyName(changes.getCompanyName());
            emp.setPosition(changes.getPosition());
            emp.setSalary(changes.getSalary());
            emp.setStatus(changes.getStatus());
        });
        return empOpt;
    }
    public Optional<Employee> updateStatusByEmail(String email, EmploymentStatus status){
        if (email==null || email.isBlank() || status==null) return Optional.empty();
        Optional<Employee> empOpt = GetEmployeeByEmail(email);
        empOpt.ifPresent(emp -> emp.setStatus(status));
        return empOpt;
    }
    public boolean deleteEmployeeByEmail(String email) {
        if (email==null || email.isBlank()) return false;
        return employees.removeIf(emp -> email.equalsIgnoreCase(emp.getEmail()));
    }

    public void displayAll(){
       if (employees.isEmpty()){
           System.out.println("No employees to display.");
           return;
       }
       employees.forEach(System.out::println);
    }

    public List<Employee> getEmployeeByCompany(String companyName){
        if (companyName==null || companyName.isBlank())
            return Collections.emptyList();
        return employees.stream()
                .filter(emp ->
                        emp.getCompanyName() != null &&
                        emp.getCompanyName().equalsIgnoreCase(companyName))
                .collect(Collectors.toList());
    }

    public double getAverageSalaryByCompany(String companyName) {
        return getEmployeeByCompany(companyName).stream().mapToInt(Employee::getSalary).average().orElse(0.0);
    }

    public List<Employee> getByStatus(EmploymentStatus status) {
        if (status == null) return Collections.emptyList();
        return employees.stream().filter(e -> e.getStatus() == status).collect(Collectors.toList());
    }

    public Map<EmploymentStatus, Integer> countByStatus() {
        return employees.stream().collect(Collectors.groupingBy(Employee::getStatus, Collectors.summingInt(e -> 1)));
    }

    public List<Employee> getAlphabetically(){
        return employees.stream()
                .sorted(Comparator
                        .comparing(Employee::getLastName)
                        .thenComparing(Employee::getFirstName))
                .collect(Collectors.toList());
    }

    public Map<Position, List<Employee>> groupByPosition(){
        return employees.stream().collect(Collectors.groupingBy(Employee::getPosition));
    }

    public Map<Position, Integer> countByPosition(){
        return employees.stream().collect(Collectors.groupingBy(Employee::getPosition, Collectors.summingInt(e -> 1)));
    }

    public double getAverageSalary() {
        return employees.stream()
                .mapToInt(Employee::getSalary)
                .average()
                .orElse(0.0);
    }

    public Optional<Employee> getHighestSalary() {
        return employees.stream().max(Comparator.comparingInt(Employee::getSalary));
    }

    public List<Employee> validateSalaryConsistency(){
        return employees.stream()
                .filter(Objects::nonNull)
                .filter(emp -> emp.getPosition() != null)
                .filter(emp -> emp.getSalary() < emp.getPosition().getSalary())
                .collect(Collectors.toList());
    }

    public Map<String, CompanyStatistics> getCompanyStatistics() {
        return employees.stream()
                .filter(Objects::nonNull)
                .filter(emp -> emp.getCompanyName() != null && !emp.getCompanyName().isBlank())
                .collect(Collectors.groupingBy(
                        Employee::getCompanyName,
                        Collectors.collectingAndThen(Collectors.toList(), list -> {
                            int count = list.size();
                            double avg = list.stream().mapToInt(Employee::getSalary).average().orElse(0.0);
                            Employee topEarner = list.stream()
                                    .max(Comparator.comparingInt(Employee::getSalary))
                                    .orElse(null);
                            String topEarnerName = topEarner != null ? topEarner.getFullName() : "";
                            int highestSalary = 0;
                            if (topEarnerName != null) {
                                assert topEarner != null;
                                highestSalary = topEarner.getSalary();
                            }

                            return new CompanyStatistics(count, avg, topEarnerName, highestSalary);
                        })
                ));
    }
}
